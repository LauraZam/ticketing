package kz.kimep.ticketing.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisLuaConfig {

    private static final String HOLD_LUA_SCRIPT =
            "for i, key in ipairs(KEYS) do " +
                    "    if redis.call('EXISTS', key) == 1 then " +
                    "        return 0 " +
                    "    end " +
                    "end " +
                    "for i, key in ipairs(KEYS) do " +
                    "    redis.call('SET', key, ARGV[1], 'EX', tonumber(ARGV[2])) " +
                    "end " +
                    "return 1";

    @Bean
    public RedisScript<Long> seatHoldScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(HOLD_LUA_SCRIPT);
        script.setResultType(Long.class);
        return script;
    }
}