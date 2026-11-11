package kz.kimep.ticketing.catalog.api;

import kz.kimep.ticketing.catalog.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class EventDetailResponse {
    private Long id;
    private String title;
    private String description;
    private Instant startsAt;
    private Instant doorsOpenAt;
    private EventStatus status;
    private Instant salesOpenAt;
    private Instant salesCloseAt;
    private String venueName;
    private String venueAddress;
    private List<SectionPriceResponse> prices;
}