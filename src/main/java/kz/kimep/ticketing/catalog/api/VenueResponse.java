package kz.kimep.ticketing.catalog.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VenueResponse {
    private Long id;
    private String name;
    private String addressLine;
    private String city;
    private String timezone;
    private Integer totalCapacity;
}