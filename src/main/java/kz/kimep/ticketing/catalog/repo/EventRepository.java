package kz.kimep.ticketing.catalog.repo;

import kz.kimep.ticketing.catalog.domain.Event;
import kz.kimep.ticketing.catalog.domain.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"venue"})
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"venue", "prices", "prices.section"})
    Optional<Event> findById(Long id);
}