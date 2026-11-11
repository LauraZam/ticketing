package kz.kimep.ticketing.messaging.service;

import kz.kimep.ticketing.messaging.config.RabbitMQConfig;
import kz.kimep.ticketing.messaging.domain.OutboxEvent;
import kz.kimep.ticketing.messaging.repo.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private static final long FIXED_DELAY_MS = 3000L;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = FIXED_DELAY_MS)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findTop50ByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pendingEvents) {
            publishSingleEvent(event);
        }
    }

    private void publishSingleEvent(OutboxEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.BOOKING_ROUTING_KEY,
                    event.getPayload()
            );
            event.setProcessed(true);
            event.setProcessedAt(Instant.now());
            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to publish outbox event id: {}", event.getId(), e);
        }
    }
}