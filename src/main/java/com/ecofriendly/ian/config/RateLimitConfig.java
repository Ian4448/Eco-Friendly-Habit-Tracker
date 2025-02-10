package com.ecofriendly.ian.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {
    @Bean
    public Bucket createNewBucket() {
        long capacity = 300; // maximum tokens
        long refillTokens = 300; // tokens to refill
        long refillDuration = 1; // duration unit in minutes

        Refill refill = Refill.intervally(refillTokens, Duration.ofMinutes(refillDuration));
        Bandwidth limit = Bandwidth.classic(capacity, refill);

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
