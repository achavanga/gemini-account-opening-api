package nl.co.geminibank.accountopening.entity.repository;

import nl.co.geminibank.accountopening.entity.model.Customer;
import nl.co.geminibank.accountopening.entity.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Transactional
public interface AccountOpeningRepository extends JpaRepository<Customer, String> {

    Optional<Customer> findCustomerByRequestId(String requestId);

    List<Customer> findByStatusAndPausedAtBefore(RequestStatus requestStatus, OffsetDateTime expirationTime);

}
