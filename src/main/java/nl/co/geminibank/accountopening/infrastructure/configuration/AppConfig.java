package nl.co.geminibank.accountopening.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Bean
    public Clock systemClock() {
        return Clock.system(ZoneId.of("Europe/Amsterdam"));
    }
}
