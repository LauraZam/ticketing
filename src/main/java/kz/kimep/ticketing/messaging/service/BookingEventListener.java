package kz.kimep.ticketing.messaging.service;

import kz.kimep.ticketing.booking.domain.BookingCreatedEvent;
import kz.kimep.ticketing.messaging.domain.OutboxEvent;
import kz.kimep.ticketing.messaging.repo.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookingEventListener {

    private final OutboxEventRepository outboxEventRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleBookingCreated(BookingCreatedEvent event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("BOOKING");
        outboxEvent.setAggregateId(event.getBookingReference());
        outboxEvent.setEventType("BOOKING_CREATED");
        outboxEvent.setPayload(buildPayload(event));
        outboxEventRepository.save(outboxEvent);
    }

    private String buildPayload(BookingCreatedEvent event) {
        return String.format(
                "{\"bookingReference\":\"%s\",\"userId\":%d,\"eventId\":%d,\"totalAmount\":%s}",
                event.getBookingReference(),
                event.getUserId(),
                event.getEventId(),
                event.getTotalAmount().toString()
        );
    }
}