package nl.co.geminibank.accountopening.control.service;

import nl.co.geminibank.accountopening.entity.model.Customer;
import nl.co.geminibank.accountopening.entity.model.RequestStatus;
import nl.co.geminibank.accountopening.entity.repository.AccountOpeningRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Profile("!test-no-scheduling")
public class ExpirePausedRequestService {
    private static final Logger log = LoggerFactory.getLogger(ExpirePausedRequestService.class);
    public static final int EXPIRED_DAYS = 7;
    private final AccountOpeningRepository accountOpeningRepository;
    private final Clock clock;

    public ExpirePausedRequestService(AccountOpeningRepository accountOpeningRepository, Clock clock) {
        this.accountOpeningRepository = accountOpeningRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 20 * * ?") // Run daily
    public void expirePausedRequests() {
        log.info("Expire paused account registration");
        // Implement thread safety as needed here
        OffsetDateTime expirationTime = OffsetDateTime.now(clock).minusDays(EXPIRED_DAYS);
        List<Customer> expiredRequests = accountOpeningRepository
                .findByStatusAndPausedAtBefore(RequestStatus.PAUSED, expirationTime);

        expiredRequests.forEach(request -> {
            request.setStatus(RequestStatus.EXPIRED);
            accountOpeningRepository.save(request);
        });
        log.info("Expire paused {} requests", expiredRequests.size());
    }
}
