package nl.co.geminibank.accountopening.control.service;

import nl.co.geminibank.accountopening.entity.model.Customer;
import nl.co.geminibank.accountopening.entity.model.RequestStatus;
import nl.co.geminibank.accountopening.entity.repository.AccountOpeningRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpirePausedRequestServiceTest {

    @Mock
    private AccountOpeningRepository accountOpeningRepository;

    @InjectMocks
    private ExpirePausedRequestService expirePausedRequestService;

    @Test
    void shouldExpirePausedRequestsWhenCalled() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-02-03T12:00:00Z"), ZoneId.of("Europe/Amsterdam"));
        expirePausedRequestService = new ExpirePausedRequestService(accountOpeningRepository, fixedClock);

        Customer customer = new Customer();
        customer.setStatus(RequestStatus.PAUSED);
        customer.setPausedAt(OffsetDateTime.now(fixedClock).minusDays(8)); // 8 days ago, expired
        List<Customer> pausedRequests = Collections.singletonList(customer);

        when(accountOpeningRepository.findByStatusAndPausedAtBefore(eq(RequestStatus.PAUSED), any(OffsetDateTime.class)))
                .thenReturn(pausedRequests);

        expirePausedRequestService.expirePausedRequests();
        assertThat(pausedRequests.get(0).getStatus()).isEqualTo(RequestStatus.EXPIRED); // Check status changed to EXPIRED
        verify(accountOpeningRepository).save(customer); // Verify save was called for the updated status
    }

    @Test
    void shouldNotExpireRequestsWhenNoneAreExpired() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-02-03T12:00:00Z"), ZoneId.of("Europe/Amsterdam"));
        expirePausedRequestService = new ExpirePausedRequestService(accountOpeningRepository, fixedClock);

        when(accountOpeningRepository.findByStatusAndPausedAtBefore(eq(RequestStatus.PAUSED), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList());

        expirePausedRequestService.expirePausedRequests();
        verify(accountOpeningRepository, never()).save(any()); //
    }
}
