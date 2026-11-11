package kz.kimep.ticketing.booking.repo;

import kz.kimep.ticketing.booking.domain.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findByHoldReference(String holdReference);
}