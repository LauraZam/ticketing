package kz.kimep.ticketing.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "ticketing.events";
    public static final String BOOKING_QUEUE = "booking.notifications.queue";
    public static final String BOOKING_ROUTING_KEY = "booking.created";

    @Bean
    public TopicExchange ticketingExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue bookingQueue() {
        return new Queue(BOOKING_QUEUE, true);
    }

    @Bean
    public Binding bookingBinding(Queue bookingQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(bookingQueue).to(ticketingExchange).with(BOOKING_ROUTING_KEY);
    }
}