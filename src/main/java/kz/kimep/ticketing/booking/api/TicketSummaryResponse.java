package kz.kimep.ticketing.booking.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TicketSummaryResponse {
    private String ticketCode;
    private Long seatId;
    private String rowLabel;
    private Integer seatNumber;
    private BigDecimal pricePaid;
}