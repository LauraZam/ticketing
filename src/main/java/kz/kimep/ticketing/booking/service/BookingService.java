package kz.kimep.ticketing.booking.service;

import kz.kimep.ticketing.booking.api.BookingResponse;
import kz.kimep.ticketing.booking.api.CreateBookingRequest;
import kz.kimep.ticketing.booking.api.TicketSummaryResponse;
import kz.kimep.ticketing.booking.domain.Booking;
import kz.kimep.ticketing.booking.domain.BookingCreatedEvent;
import kz.kimep.ticketing.booking.domain.BookingStatus;
import kz.kimep.ticketing.booking.domain.SeatHold;
import kz.kimep.ticketing.booking.domain.SeatHoldStatus;
import kz.kimep.ticketing.booking.domain.Ticket;
import kz.kimep.ticketing.booking.domain.TicketStatus;
import kz.kimep.ticketing.booking.repo.BookingRepository;
import kz.kimep.ticketing.booking.repo.SeatHoldRepository;
import kz.kimep.ticketing.catalog.domain.EventSectionPrice;
import kz.kimep.ticketing.catalog.domain.Seat;
import kz.kimep.ticketing.catalog.service.CatalogService;
import kz.kimep.ticketing.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");
    private static final int REF_START = 0;
    private static final int REF_END = 8;

    private final SeatHoldRepository seatHoldRepository;
    private final BookingRepository bookingRepository;
    private final CatalogService catalogService;
    private final PaymentService paymentService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Long userId) {
        SeatHold hold = validateHold(request.getHoldReference(), userId);
        Map<Long, BigDecimal> seatPrices = loadSeatPrices(hold);

        BigDecimal subtotal = calculateSubtotal(hold.getSeats(), seatPrices);
        BigDecimal fees = subtotal.multiply(SERVICE_FEE_RATE);
        BigDecimal total = subtotal.add(fees);

        Booking booking = buildBooking(hold, subtotal, fees, total);
        List<Ticket> tickets = createTickets(booking, hold, seatPrices);
        booking.setTickets(tickets);

        Booking savedBooking = bookingRepository.save(booking);
        paymentService.processPayment(savedBooking, request.getPaymentMethod(), total);
        hold.setStatus(SeatHoldStatus.CONSUMED);

        eventPublisher.publishEvent(new BookingCreatedEvent(
                savedBooking.getBookingReference(),
                savedBooking.getUser().getId(),
                savedBooking.getEvent().getId(),
                savedBooking.getTotal()
        ));

        return mapToResponse(savedBooking);
    }

    private SeatHold validateHold(String ref, Long userId) {
        SeatHold hold = seatHoldRepository.findByHoldReference(ref)
                .orElseThrow(() -> new IllegalArgumentException("Hold reference not found"));
        if (!hold.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized hold reference");
        }
        if (hold.getStatus() != SeatHoldStatus.ACTIVE || hold.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Hold is no longer active");
        }
        return hold;
    }

    private Map<Long, BigDecimal> loadSeatPrices(SeatHold hold) {
        List<EventSectionPrice> prices = catalogService.findEventPrices(hold.getEvent().getId());
        return prices.stream().collect(
                Collectors.toMap(p -> p.getSection().getId(), EventSectionPrice::getPrice)
        );
    }

    private BigDecimal calculateSubtotal(Iterable<Seat> seats, Map<Long, BigDecimal> prices) {
        BigDecimal total = BigDecimal.ZERO;
        for (Seat seat : seats) {
            BigDecimal price = prices.getOrDefault(seat.getSection().getId(), BigDecimal.ZERO);
            total = total.add(price);
        }
        return total;
    }

    private Booking buildBooking(SeatHold hold, BigDecimal subtotal, BigDecimal fees, BigDecimal total) {
        Booking booking = new Booking();
        String ref = "BK-" + UUID.randomUUID().toString()
                .substring(REF_START, REF_END).toUpperCase();
        booking.setBookingReference(ref);
        booking.setEvent(hold.getEvent());
        booking.setUser(hold.getUser());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setSubtotal(subtotal);
        booking.setFees(fees);
        booking.setTotal(total);
        booking.setConfirmedAt(Instant.now());
        return booking;
    }

    private List<Ticket> createTickets(Booking booking, SeatHold hold, Map<Long, BigDecimal> prices) {
        List<Ticket> tickets = new ArrayList<>();
        for (Seat seat : hold.getSeats()) {
            Ticket ticket = new Ticket();
            String code = "TCK-" + UUID.randomUUID().toString()
                    .substring(REF_START, REF_END).toUpperCase();
            ticket.setTicketCode(code);
            ticket.setBooking(booking);
            ticket.setSeat(seat);
            ticket.setEvent(hold.getEvent());
            ticket.setStatus(TicketStatus.ISSUED);
            ticket.setPricePaid(prices.getOrDefault(seat.getSection().getId(), BigDecimal.ZERO));
            tickets.add(ticket);
        }
        return tickets;
    }

    private BookingResponse mapToResponse(Booking booking) {
        List<TicketSummaryResponse> ticketResponses = booking.getTickets().stream()
                .map(t -> new TicketSummaryResponse(
                        t.getTicketCode(),
                        t.getSeat().getId(),
                        t.getSeat().getRowLabel(),
                        t.getSeat().getSeatNumber(),
                        t.getPricePaid()
                )).toList();

        return BookingResponse.builder()
                .bookingReference(booking.getBookingReference())
                .eventId(booking.getEvent().getId())
                .eventTitle(booking.getEvent().getTitle())
                .status(booking.getStatus())
                .subtotal(booking.getSubtotal())
                .fees(booking.getFees())
                .total(booking.getTotal())
                .currency(booking.getCurrency())
                .createdAt(booking.getCreatedAt())
                .tickets(ticketResponses)
                .build();
    }
}