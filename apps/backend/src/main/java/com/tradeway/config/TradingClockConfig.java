package com.tradeway.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradingClockConfig {

    @Bean("defaultMarketClock")
    public Clock tradewayClock() {
        return Clock.system(ZoneId.of("America/New_York"));
    }
}
