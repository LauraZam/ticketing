package kz.kimep.ticketing.messaging.repo;

import kz.kimep.ticketing.messaging.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop50ByProcessedFalseOrderByCreatedAtAsc();
}