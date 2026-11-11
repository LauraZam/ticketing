package kz.kimep.ticketing.catalog.api;

import kz.kimep.ticketing.catalog.domain.EventStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class EventSummaryResponse {
    private Long id;
    private String title;
    private Instant startsAt;
    private EventStatus status;
    private String venueName;
    private String city;
}