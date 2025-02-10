package nl.co.geminibank.accountopening.control.service;

import nl.co.geminibank.accountopening.control.exception.CustomerApplicationIsSubmittedException;
import nl.co.geminibank.accountopening.control.exception.CustomerException;
import nl.co.geminibank.accountopening.control.exception.CustomerNotFoundException;
import nl.co.geminibank.accountopening.entity.model.AccountType;
import nl.co.geminibank.accountopening.entity.model.Customer;
import nl.co.geminibank.accountopening.entity.model.RequestStatus;
import nl.co.geminibank.accountopening.entity.repository.AccountOpeningRepository;
import nl.co.geminibank.accountopening.model.AccountTypeDTO;
import nl.co.geminibank.accountopening.model.AddressDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestResponseDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestStartDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestUpdateDTO;
import nl.co.geminibank.accountopening.model.RequestStatusDTO;
import nl.co.geminibank.accountopening.util.RequestGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountOpeningServiceTest {

    private static final String REQUEST_ID = "1234";
    private static final String EMAIL = "customer@example.com";
    private static final String NAME = "John Doe";

    @Mock
    private AccountOpeningRepository accountOpeningRepository;

    @Mock
    private RequestGenerator requestGenerator;

    private AccountOpeningService accountOpeningService;
    private CustomerRequestStartDTO customerRequestStartDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        ModelMapper modelMapper = new ModelMapper();
        accountOpeningService = new AccountOpeningService(accountOpeningRepository, modelMapper, requestGenerator);
        createcustomerRequestStartDTO();
        createCustomerData();
    }

    @Test
    void shouldStartAccountRegistration() {
        when(requestGenerator.generateRequestId()).thenReturn(REQUEST_ID);
        CustomerRequestResponseDTO response = accountOpeningService.startAccountRegistration(customerRequestStartDTO);
        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).contains(REQUEST_ID);
        assertThat(response.getEmail()).contains(EMAIL);
        assertThat(response.getName()).contains(NAME);
    }

    @Test
    void shouldPauseAccountRegistration() {
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.of(customer));
        CustomerRequestResponseDTO response = accountOpeningService.pauseAccountRegistration(REQUEST_ID);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).contains(RequestStatusDTO.PAUSED);
        assertThat(response.getEmail()).contains(EMAIL);
        assertThat(response.getName()).contains(NAME);

        // Ensure the pause timestamp is set correctly
        assertThat(customer.getPausedAt()).isNotNull();
        assertThat(customer.getPausedAt()).isBeforeOrEqualTo(OffsetDateTime.now());
    }

    @Test
    void shouldThrowExceptionWhenPauseIsAttemptedOnSubmittedCustomer() {
        customer.setStatus(RequestStatus.SUBMITTED);
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.of(customer));
        assertThatThrownBy(() -> accountOpeningService.pauseAccountRegistration(REQUEST_ID))
                .isInstanceOf(CustomerApplicationIsSubmittedException.class)
                .hasMessageContaining("Customer application for request id [" + REQUEST_ID + "] cannot be paused");
    }

    @Test
    void shouldThrowExceptionWhenResumingAnAccountRegistrationWhichIsNotPaused() {
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.of(customer));
        assertThatThrownBy(() -> accountOpeningService.resumeAccountRegistration(REQUEST_ID,customerRequestStartDTO))
                .isInstanceOf(CustomerException.class)
                .hasMessageContaining("Customer application for request id [" + REQUEST_ID + "] cannot be resumed, because its not in paused status");
    }

    @Test
    void shouldResumingAnAccountRegistration() {
        customer.setStatus(RequestStatus.PAUSED);
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.of(customer));

        CustomerRequestUpdateDTO response = accountOpeningService.resumeAccountRegistration(REQUEST_ID, customerRequestStartDTO);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).contains(RequestStatusDTO.SUBMITTED);
        assertThat(response.getEmail()).contains(EMAIL);
        assertThat(response.getAccountType()).contains(AccountTypeDTO.CURRENT);

        verify(accountOpeningRepository).save(customer);
    }

    @Test
    void shouldGetCustomerAccountRegistrationById() {
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.of(customer));
        CustomerRequestResponseDTO response = accountOpeningService.getCustomerAccountRegistrationById(REQUEST_ID);
        assertThat(response).isNotNull();
        assertThat(response.getRequestId()).contains(REQUEST_ID);
        assertThat(response.getStatus()).contains(RequestStatusDTO.IN_PROGRESS);
        assertThat(response.getEmail()).contains(EMAIL);
        assertThat(response.getAccountType()).contains(AccountTypeDTO.CURRENT);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(accountOpeningRepository.findCustomerByRequestId(REQUEST_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> accountOpeningService.getCustomerAccountRegistrationById(REQUEST_ID))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with request id [" + REQUEST_ID + "] not found.");
    }

    private void createCustomerData() {
        // Prepare Customer entity with initial test data
        customer = new Customer();
        customer.setId(1L);
        customer.setName(NAME);
        customer.setEmail(EMAIL);
        customer.setAccountType(AccountType.CURRENT);
        customer.setRequestId(REQUEST_ID);
        customer.setStatus(RequestStatus.IN_PROGRESS);
        customer.setPausedAt(null); // Initially not paused
    }

    private void createcustomerRequestStartDTO() {
        // Prepare CustomerRequestStartDTO with test data
        customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setAccountType(Optional.of(AccountTypeDTO.CURRENT));
        customerRequestStartDTO.setEmail(Optional.of(EMAIL));
        customerRequestStartDTO.setMonthlySalary(Optional.of(100.00));
        customerRequestStartDTO.setIdDocument(Optional.of("123456789"));
        customerRequestStartDTO.setStartingBalance(Optional.of(1000.00));
        customerRequestStartDTO.setName(NAME);
        customerRequestStartDTO.setAddress(new AddressDTO("Street 2", "4", "1234 AB", "City"));
        customerRequestStartDTO.setDateOfBirth(LocalDate.of(1990, 5, 20));
    }

}
