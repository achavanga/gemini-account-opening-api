package nl.co.geminibank.accountopening.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class RequestGenerator {
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateRequestId() {
        secureRandom.setSeed(System.currentTimeMillis());

        return Long.toHexString(secureRandom.nextLong());
    }
}
