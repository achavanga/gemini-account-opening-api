package nl.co.geminibank.accountopening.control.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.co.geminibank.accountopening.boundary.exception.CustomerApplicationIsSubmittedException;
import nl.co.geminibank.accountopening.boundary.exception.CustomerException;
import nl.co.geminibank.accountopening.boundary.exception.CustomerNotFoundException;
import nl.co.geminibank.accountopening.entity.model.Customer;
import nl.co.geminibank.accountopening.entity.model.RequestStatus;
import nl.co.geminibank.accountopening.entity.repository.AccountOpeningRepository;
import nl.co.geminibank.accountopening.model.CustomerRequestResponseDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestStartDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestUpdateDTO;
import nl.co.geminibank.accountopening.util.RequestGenerator;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountOpeningService {
    private static final Logger log = LoggerFactory.getLogger(AccountOpeningService.class);
    private final AccountOpeningRepository accountOpeningRepository;
    private final ModelMapper modelMapper;
    private final RequestGenerator requestGenerator;

    /**
     * Starts the account registration process by generating a request ID and saving the customer.
     *
     * @param customerRequestStartDTO the request DTO containing customer details.
     * @return the {@link CustomerRequestResponseDTO} containing the registration response.
     */
    public CustomerRequestResponseDTO startAccountRegistration(@Valid CustomerRequestStartDTO customerRequestStartDTO) {
        log.info("Starting account registration");
        var customer = createAndSaveCustomer(customerRequestStartDTO);
        return mapToResponseDTO(customer, CustomerRequestResponseDTO.class);
    }

    /**
     * Pauses the account registration process for the customer.
     *
     * @param requestId the unique identifier for the customer request.
     * @return the {@link CustomerRequestResponseDTO} containing the pause response.
     */
    public CustomerRequestResponseDTO pauseAccountRegistration(String requestId) {
        log.info("Pausing account registration");
        var customer = findCustomerByRequestId(requestId);
        checkIfCustomerApplicationIsSubmitted(customer, requestId);
        customer.pause();
        accountOpeningRepository.save(customer);
        return mapToResponseDTO(customer, CustomerRequestResponseDTO.class);
    }

    /**
     * Resumes the account registration process for the customer by updating their details.
     *
     * @param requestId the unique identifier for the customer request.
     * @param customerRequestStartDTO the DTO containing updated customer details.
     * @return the {@link CustomerRequestUpdateDTO} containing the updated registration response.
     */
    public CustomerRequestUpdateDTO resumeAccountRegistration(String requestId, CustomerRequestStartDTO customerRequestStartDTO) {
        log.info("Resuming account registration");
        var customer = findCustomerByRequestId(requestId);
        checkIfCustomerRequestIsPaused(customer, requestId);
        updateCustomerDetails(customer, customerRequestStartDTO);
        accountOpeningRepository.save(customer);
        return mapToResponseDTO(customer, CustomerRequestUpdateDTO.class);
    }

    /**
     * Retrieves the customer account registration by request ID.
     *
     * @param requestId the unique identifier for the customer request.
     * @return the {@link CustomerRequestResponseDTO} containing the customer details.
     */
    public CustomerRequestResponseDTO getCustomerAccountRegistrationById(String requestId) {
        var customer = findCustomerByRequestId(requestId);
        return mapToResponseDTO(customer, CustomerRequestResponseDTO.class);
    }

    /**
     * Creates a new customer entity and saves it.
     *
     * @param customerRequestStartDTO the DTO containing customer details.
     * @return the newly created customer entity.
     */
    private Customer createAndSaveCustomer(CustomerRequestStartDTO customerRequestStartDTO) {
        log.info("Creating account customer");
        var requestID = requestGenerator.generateRequestId();
        modelMapper.typeMap(CustomerRequestStartDTO.class, Customer.class)
                .addMappings(mapper -> mapper.skip(Customer::setId));  // Skip setting the ID field

        var customer = modelMapper.map(customerRequestStartDTO, Customer.class);
        customer.setRequestId(requestID);
        customer.setStatus(RequestStatus.IN_PROGRESS);
        accountOpeningRepository.save(customer);
        log.info("Created account registration  {}", requestID);
        return customer;
    }

    /**
     * Finds a customer by their request ID.
     *
     * @param requestId the unique identifier for the customer request.
     * @return the customer entity.
     */
    private Customer findCustomerByRequestId(String requestId) {
        return accountOpeningRepository.findCustomerByRequestId(requestId)
                .orElseThrow(() -> new CustomerNotFoundException(requestId,
                        "Customer with request id [%s] not found.".formatted(requestId)));
    }

    /**
     * Checks if the customer registration is submitted.
     *
     * @param customer the customer entity.
     * @param requestId the unique identifier for the customer request.
     */
    private void checkIfCustomerApplicationIsSubmitted(Customer customer, String requestId) {
        if (customer.getStatus().equals(RequestStatus.SUBMITTED)) {
            throw new CustomerApplicationIsSubmittedException(requestId,
                    "Customer application for request id [%s] cannot be paused / resumed, it is already submitted.".formatted(requestId));
        }
    }

    /**
     * Checks if the customer's request is in a paused status. If the request is not paused,
     * a {@link CustomerException} is thrown with an appropriate error message.
     *
     * <p>
     * This method is used to ensure that certain operations (e.g., resuming a request) can only
     * be performed when the customer's request is in the {@link RequestStatus#PAUSED} status.
     * </p>
     *
     * @param customer  the customer whose request status is being checked. Must not be {@code null}.
     * @param requestId the unique identifier of the customer's request. Used in the error message
     *                  if the request is not paused. Must not be {@code null} or empty.
     *
     * @throws CustomerException if the customer's request is not in the {@link RequestStatus#PAUSED} status.
     *                           The exception message includes the {@code requestId} for reference.
     * @see Customer
     * @see RequestStatus
     * @see CustomerException
     */
    private void checkIfCustomerRequestIsPaused(Customer customer, String requestId) {
        if (!customer.getStatus().equals(RequestStatus.PAUSED)) {
            throw new CustomerException(requestId,
                    "Customer application for request id [%s] cannot be resumed, because its not in paused status".formatted(requestId));
        }
    }

    /**
     * Updates customer details from the provided DTO.
     *
     * @param customer the customer entity to update.
     * @param customerRequestStartDTO the DTO containing updated customer details.
     */
    private void updateCustomerDetails(Customer customer, CustomerRequestStartDTO customerRequestStartDTO) {
        modelMapper.typeMap(CustomerRequestStartDTO.class, Customer.class)
                .addMappings(mapper -> mapper.skip(Customer::setId));  // Skip setting the ID field

        var updatedCustomer = modelMapper.map(customerRequestStartDTO, Customer.class);
        customer.setAccountType(updatedCustomer.getAccountType());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setIdDocument(updatedCustomer.getIdDocument());
        customer.setMonthlySalary(updatedCustomer.getMonthlySalary());
        customer.setInterestedInOtherProducts(updatedCustomer.getInterestedInOtherProducts());
        customer.setStartingBalance(updatedCustomer.getStartingBalance());
        customer.setPausedAt(null);
        customer.setStatus(RequestStatus.SUBMITTED);
    }
    /**
     * Maps the given customer entity to the specified DTO.
     *
     * @param customer the customer entity.
     * @param dtoClass the DTO class to map to.
     * @param <T> the type of DTO.
     * @return the mapped DTO.
     */
    private <T> T mapToResponseDTO(Customer customer, Class<T> dtoClass) {
        return modelMapper.map(customer, dtoClass);
    }
}
