package kz.kimep.ticketing.catalog.api;

import kz.kimep.ticketing.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/venues")
@RequiredArgsConstructor
public class VenueController {

    private final CatalogService catalogService;

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getVenues() {
        return ResponseEntity.ok(catalogService.getAllVenues());
    }

    @GetMapping("/{id}/seats")
    public ResponseEntity<List<SeatResponse>> getVenueSeats(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getVenueSeats(id));
    }
}