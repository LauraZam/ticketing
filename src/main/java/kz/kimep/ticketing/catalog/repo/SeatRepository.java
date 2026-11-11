package kz.kimep.ticketing.catalog.repo;

import kz.kimep.ticketing.catalog.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySectionVenueId(Long venueId);
}