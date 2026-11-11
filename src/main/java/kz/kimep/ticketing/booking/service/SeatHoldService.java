package kz.kimep.ticketing.booking.service;

import kz.kimep.ticketing.booking.api.CreateHoldRequest;
import kz.kimep.ticketing.booking.api.HoldResponse;
import kz.kimep.ticketing.booking.domain.SeatHold;
import kz.kimep.ticketing.booking.domain.SeatHoldStatus;
import kz.kimep.ticketing.booking.repo.SeatHoldRepository;
import kz.kimep.ticketing.catalog.domain.Event;
import kz.kimep.ticketing.catalog.domain.Seat;
import kz.kimep.ticketing.catalog.service.CatalogService;
import kz.kimep.ticketing.identity.domain.User;
import kz.kimep.ticketing.identity.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private static final int HOLD_TTL_MINUTES = 10;
    private static final int HOLD_TTL_SECONDS = 600;
    private static final int REF_START = 0;
    private static final int REF_END = 8;

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> seatHoldScript;
    private final SeatHoldRepository seatHoldRepository;
    private final CatalogService catalogService;
    private final UserRepository userRepository;

    @Transactional
    public HoldResponse holdSeats(CreateHoldRequest request, Long userId) {
        Event event = catalogService.findEventEntityById(request.getEventId());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Seat> seats = validateAndFetchSeats(request.getSeatIds());
        String holdRef = "HOLD-" + UUID.randomUUID().toString()
                .substring(REF_START, REF_END).toUpperCase();

        acquireRedisLock(request.getEventId(), request.getSeatIds(), holdRef);

        SeatHold hold = createAndSaveHold(holdRef, event, user, seats);
        return mapToResponse(hold);
    }

    private List<Seat> validateAndFetchSeats(Set<Long> seatIds) {
        List<Seat> seats = catalogService.findSeatsByIds(seatIds);
        if (seats.size() != seatIds.size()) {
            throw new IllegalArgumentException("One or more invalid seat IDs");
        }
        return seats;
    }

    private void acquireRedisLock(Long eventId, Set<Long> seatIds, String holdRef) {
        List<String> redisKeys = seatIds.stream()
                .map(seatId -> "event:" + eventId + ":seat:" + seatId)
                .toList();

        Long lockResult = redisTemplate.execute(
                seatHoldScript, redisKeys, holdRef, String.valueOf(HOLD_TTL_SECONDS));

        if (lockResult == null || lockResult == 0L) {
            throw new IllegalStateException("One or more seats are already held");
        }
    }

    private SeatHold createAndSaveHold(String ref, Event event, User user, List<Seat> seats) {
        SeatHold hold = new SeatHold();
        hold.setHoldReference(ref);
        hold.setEvent(event);
        hold.setUser(user);
        hold.setStatus(SeatHoldStatus.ACTIVE);
        hold.setExpiresAt(Instant.now().plus(HOLD_TTL_MINUTES, ChronoUnit.MINUTES));
        hold.setSeats(new HashSet<>(seats));
        return seatHoldRepository.save(hold);
    }

    private HoldResponse mapToResponse(SeatHold hold) {
        Set<Long> seatIds = hold.getSeats().stream()
                .map(Seat::getId)
                .collect(Collectors.toSet());

        return HoldResponse.builder()
                .holdReference(hold.getHoldReference())
                .eventId(hold.getEvent().getId())
                .userId(hold.getUser().getId())
                .seatIds(seatIds)
                .expiresAt(hold.getExpiresAt())
                .status(hold.getStatus())
                .build();
    }
}