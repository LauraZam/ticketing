package kz.kimep.ticketing;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TicketingApplicationTests {

	@MockBean
	private StringRedisTemplate redisTemplate;

	@MockBean
	private RabbitTemplate rabbitTemplate;

	@Test
	void contextLoads() {
	}
}