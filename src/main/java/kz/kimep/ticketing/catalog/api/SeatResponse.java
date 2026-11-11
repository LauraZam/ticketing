package kz.kimep.ticketing.catalog.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private Long sectionId;
    private String sectionName;
    private String rowLabel;
    private Integer seatNumber;
    private boolean accessible;
}