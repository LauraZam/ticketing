package kz.kimep.ticketing.messaging.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kimep.ticketing.messaging.api.BookingNotificationPayload;
import kz.kimep.ticketing.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingNotificationConsumer {

    private static final String IDEMPOTENCY_PREFIX = "processed_msg:";
    private static final Duration DEDUP_TTL = Duration.ofDays(1);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.BOOKING_QUEUE)
    public void processBookingNotification(String messageJson) {
        try {
            BookingNotificationPayload payload = objectMapper.readValue(
                    messageJson, BookingNotificationPayload.class);

            String redisKey = IDEMPOTENCY_PREFIX + payload.getBookingReference();
            Boolean isFirstTime = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, "PROCESSED", DEDUP_TTL);

            if (Boolean.FALSE.equals(isFirstTime)) {
                log.info("Duplicate event ignored for booking: {}", payload.getBookingReference());
                return;
            }

            log.info("Processed notification for Booking: {} (User: {}, Event: {}, Amount: {})",
                    payload.getBookingReference(),
                    payload.getUserId(),
                    payload.getEventId(),
                    payload.getTotalAmount());

        } catch (Exception e) {
            log.error("Failed to process incoming booking notification message", e);
        }
    }
}