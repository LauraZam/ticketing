package kz.kimep.ticketing.booking.api;

import kz.kimep.ticketing.booking.domain.BookingStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class BookingResponse {
    private String bookingReference;
    private Long eventId;
    private String eventTitle;
    private BookingStatus status;
    private BigDecimal subtotal;
    private BigDecimal fees;
    private BigDecimal total;
    private String currency;
    private Instant createdAt;
    private List<TicketSummaryResponse> tickets;
}