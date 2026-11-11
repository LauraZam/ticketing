package kz.kimep.ticketing.catalog.api;

import kz.kimep.ticketing.catalog.domain.EventStatus;
import kz.kimep.ticketing.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<Page<EventSummaryResponse>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @PageableDefault(size = 10, sort = "startsAt") Pageable pageable) {
        return ResponseEntity.ok(catalogService.getEvents(status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getEventById(id));
    }
}