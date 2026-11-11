package kz.kimep.ticketing.catalog.repo;

import kz.kimep.ticketing.catalog.domain.EventSectionPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventSectionPriceRepository extends JpaRepository<EventSectionPrice, Long> {

    List<EventSectionPrice> findByEventId(Long eventId);
}