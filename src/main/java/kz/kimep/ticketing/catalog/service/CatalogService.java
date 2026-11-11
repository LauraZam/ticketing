package kz.kimep.ticketing.catalog.service;

import kz.kimep.ticketing.catalog.api.EventDetailResponse;
import kz.kimep.ticketing.catalog.api.EventSummaryResponse;
import kz.kimep.ticketing.catalog.api.SectionPriceResponse;
import kz.kimep.ticketing.catalog.api.SeatResponse;
import kz.kimep.ticketing.catalog.api.VenueResponse;
import kz.kimep.ticketing.catalog.domain.Event;
import kz.kimep.ticketing.catalog.domain.EventSectionPrice;
import kz.kimep.ticketing.catalog.domain.EventStatus;
import kz.kimep.ticketing.catalog.domain.Seat;
import kz.kimep.ticketing.catalog.domain.Venue;
import kz.kimep.ticketing.catalog.repo.EventRepository;
import kz.kimep.ticketing.catalog.repo.EventSectionPriceRepository;
import kz.kimep.ticketing.catalog.repo.SeatRepository;
import kz.kimep.ticketing.catalog.repo.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final EventSectionPriceRepository priceRepository;

    public Page<EventSummaryResponse> getEvents(EventStatus status, Pageable pageable) {
        Page<Event> events = (status != null)
                ? eventRepository.findByStatus(status, pageable)
                : eventRepository.findAll(pageable);

        return events.map(this::mapToSummary);
    }

    public EventDetailResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
        return mapToDetail(event);
    }

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToVenueResponse)
                .toList();
    }

    public List<SeatResponse> getVenueSeats(Long venueId) {
        return seatRepository.findBySectionVenueId(venueId).stream()
                .map(seat -> new SeatResponse(
                        seat.getId(),
                        seat.getSection().getId(),
                        seat.getSection().getName(),
                        seat.getRowLabel(),
                        seat.getSeatNumber(),
                        seat.isAccessible()
                ))
                .toList();
    }

    public Event findEventEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with id: " + id));
    }

    public List<Seat> findSeatsByIds(Set<Long> seatIds) {
        return seatRepository.findAllById(seatIds);
    }

    public List<EventSectionPrice> findEventPrices(Long eventId) {
        return priceRepository.findByEventId(eventId);
    }

    private EventSummaryResponse mapToSummary(Event event) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .startsAt(event.getStartsAt())
                .status(event.getStatus())
                .venueName(event.getVenue().getName())
                .city(event.getVenue().getCity())
                .build();
    }

    private EventDetailResponse mapToDetail(Event event) {
        List<SectionPriceResponse> prices = event.getPrices().stream()
                .map(p -> new SectionPriceResponse(
                        p.getSection().getId(),
                        p.getSection().getName(),
                        p.getSection().getPriceTier(),
                        p.getPrice(),
                        p.getCurrency()
                )).toList();

        return EventDetailResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startsAt(event.getStartsAt())
                .doorsOpenAt(event.getDoorsOpenAt())
                .status(event.getStatus())
                .salesOpenAt(event.getSalesOpenAt())
                .salesCloseAt(event.getSalesCloseAt())
                .venueName(event.getVenue().getName())
                .venueAddress(event.getVenue().getAddressLine())
                .prices(prices)
                .build();
    }

    private VenueResponse mapToVenueResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddressLine(),
                venue.getCity(),
                venue.getTimezone(),
                venue.getTotalCapacity()
        );
    }
}