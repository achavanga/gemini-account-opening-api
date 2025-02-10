package nl.co.geminibank.accountopening.boundary.controller;

import nl.co.geminibank.accountopening.TestcontainerConfig;
import nl.co.geminibank.accountopening.model.AccountTypeDTO;
import nl.co.geminibank.accountopening.model.AddressDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestResponseDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestStartDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestUpdateDTO;
import nl.co.geminibank.accountopening.model.RequestStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles({"test-no-scheduling"})
class AccountOpeningAcceptanceTest extends TestcontainerConfig {
    private static final String BASE_URL = "/v1/api/customers";
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldStartAndPauseAccountOpeningFlow() {
        // Step 1: Start a new account opening request
        CustomerRequestStartDTO customerRequestStartDTO = createCustomerRequestStartDTO();

        ResponseEntity<CustomerRequestResponseDTO> startResponse = restTemplate.postForEntity(
                BASE_URL + "/start",
                customerRequestStartDTO,
                CustomerRequestResponseDTO.class
        );

        assertAccountOpeningStarted(startResponse);

        // Step 2: Pause the request
        String requestId = startResponse.getBody().getRequestId().get();
        ResponseEntity<CustomerRequestResponseDTO> pauseResponse = restTemplate.exchange(
                BASE_URL + "/" + requestId + "/pause",
                HttpMethod.PUT,
                null, // No request body for PUT
                CustomerRequestResponseDTO.class
        );

        assertAccountOpeningPaused(pauseResponse);

        // Step 3: Resume the account opening request
        CustomerRequestStartDTO updatedRequestDTO = createUpdatedCustomerRequestStartDTO();
        ResponseEntity<CustomerRequestUpdateDTO> resumeResponse = restTemplate.exchange(
                BASE_URL + "/" + requestId + "/resume",
                HttpMethod.PUT,
                new HttpEntity<>(updatedRequestDTO),
                CustomerRequestUpdateDTO.class
        );

        // Assert account opening was resumed and updated
        assertAccountOpeningResumed(resumeResponse);

        // Step 4: Get the account opening details
        ResponseEntity<CustomerRequestResponseDTO> getResponse = restTemplate.exchange(
                BASE_URL + "/" + requestId,
                HttpMethod.GET,
                null,
                CustomerRequestResponseDTO.class
        );

        // Assert the account details were retrieved successfully
        assertAccountOpeningRetrieved(getResponse);

        // Step 5: Validation check for required fields
        CustomerRequestStartDTO invalidCustomerRequest = createInvalidCustomerRequestStartDTO();

        ResponseEntity<String> invalidResponse = restTemplate.postForEntity(
                BASE_URL + "/start",
                invalidCustomerRequest,
                String.class
        );

        // Assert validation error response
        assertValidationError(invalidResponse);
    }

    @Test
    void shouldFailToPauseNonExistentRequest() {
        // Attempt to pause a request with a non-existent ID
        ResponseEntity<String> pauseResponse = restTemplate.exchange(
                BASE_URL + "/non-existent-id/pause",
                HttpMethod.PUT,
                null,
                String.class
        );

        // Assert request not found
        assertThat(HttpStatus.NOT_FOUND).isEqualTo(pauseResponse.getStatusCode());
        assertThat(pauseResponse.getBody()).contains("not found");
    }

    @Test
    void shouldFailToResumeNonExistentRequest() {
        // Attempt to resume a request with a non-existent ID
        CustomerRequestStartDTO updatedRequestDTO = createUpdatedCustomerRequestStartDTO();
        ResponseEntity<String> resumeResponse = restTemplate.exchange(
                BASE_URL + "/non-existent-id/resume",
                HttpMethod.PUT,
                new HttpEntity<>(updatedRequestDTO),
                String.class
        );

        // Assert request not found
        assertThat(HttpStatus.NOT_FOUND).isEqualTo(resumeResponse.getStatusCode());
        assertThat(resumeResponse.getBody()).contains("not found");
    }

    @Test
    void shouldFailToResumeNonPausedRequest() {
        // Step 1: Start a new account opening request
        CustomerRequestStartDTO customerRequestStartDTO = createCustomerRequestStartDTO();
        ResponseEntity<CustomerRequestResponseDTO> startResponse = restTemplate.postForEntity(
                BASE_URL + "/start",
                customerRequestStartDTO,
                CustomerRequestResponseDTO.class
        );

        // Step 2: Attempt to resume the request without pausing it
        String requestId = startResponse.getBody().getRequestId().get();
        CustomerRequestStartDTO updatedRequestDTO = createUpdatedCustomerRequestStartDTO();
        ResponseEntity<String> resumeResponse = restTemplate.exchange(
                BASE_URL + "/" + requestId + "/resume",
                HttpMethod.PUT,
                new HttpEntity<>(updatedRequestDTO),
                String.class
        );

        // Assert resume is not allowed
        assertThat(HttpStatus.BAD_REQUEST).isEqualTo( resumeResponse.getStatusCode());
        assertThat(resumeResponse.getBody()).contains("not in paused status");
    }

    @Test
    void shouldFailToResumeWithInvalidData() {
        // Step 1: Start a new account opening request
        CustomerRequestStartDTO customerRequestStartDTO = createCustomerRequestStartDTO();
        ResponseEntity<CustomerRequestResponseDTO> startResponse = restTemplate.postForEntity(
                BASE_URL + "/start",
                customerRequestStartDTO,
                CustomerRequestResponseDTO.class
        );

        // Step 2: Pause the request
        String requestId = startResponse.getBody().getRequestId().get();
        restTemplate.exchange(BASE_URL + "/" + requestId + "/pause", HttpMethod.PUT, null, CustomerRequestResponseDTO.class);

        // Step 3: Attempt to resume with invalid data
        CustomerRequestStartDTO invalidRequestDTO = createInvalidCustomerRequestStartDTO();
        ResponseEntity<String> resumeResponse = restTemplate.exchange(
                BASE_URL + "/" + requestId + "/resume",
                HttpMethod.PUT,
                new HttpEntity<>(invalidRequestDTO),
                String.class
        );

        // Assert validation error
        assertThat(HttpStatus.BAD_REQUEST).isEqualTo( resumeResponse.getStatusCode());
        assertThat(resumeResponse.getBody()).contains("must not be null");
    }

    private CustomerRequestStartDTO createCustomerRequestStartDTO() {
        AddressDTO addressDTO = new AddressDTO("Street 1", "2", "9499 CV", "City");
        CustomerRequestStartDTO customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setName("John Doe");
        customerRequestStartDTO.setAddress(addressDTO);
        customerRequestStartDTO.setDateOfBirth(LocalDate.of(1990, 5, 20));
        return customerRequestStartDTO;
    }

    private CustomerRequestStartDTO createUpdatedCustomerRequestStartDTO() {
        AddressDTO addressDTO = new AddressDTO("Street 2", "4", "1234 AB", "City");
        CustomerRequestStartDTO customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setName("John Updated");
        customerRequestStartDTO.setAddress(addressDTO);
        customerRequestStartDTO.setDateOfBirth(LocalDate.of(1990, 5, 20));
        customerRequestStartDTO.setStartingBalance(Optional.of(100.00));
        customerRequestStartDTO.setMonthlySalary(Optional.of(1000.00));
        customerRequestStartDTO.setAccountType(Optional.of(AccountTypeDTO.CURRENT));
        customerRequestStartDTO.setIdDocument(Optional.of("12345678"));
        customerRequestStartDTO.setEmail(Optional.of("john.doe@gmail.com"));
        customerRequestStartDTO.setInterestedInOtherProducts(Optional.of(true));
        return customerRequestStartDTO;
    }

    private void assertAccountOpeningStarted(ResponseEntity<CustomerRequestResponseDTO> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getRequestId().get()).isNotNull();
        assertThat(response.getBody().getName().get()).isEqualTo("John Doe");
    }

    private void assertAccountOpeningResumed(ResponseEntity<CustomerRequestUpdateDTO> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getName().get()).isEqualTo("John Doe");
        assertThat(response.getBody().getStatus().get()).isEqualTo(RequestStatusDTO.SUBMITTED);
        assertThat(response.getBody().getAccountType().get()).isEqualTo(AccountTypeDTO.CURRENT);
    }

    private void assertAccountOpeningPaused(ResponseEntity<CustomerRequestResponseDTO> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().getStatus().get()).isEqualTo(RequestStatusDTO.PAUSED);
    }

    private void assertAccountOpeningRetrieved(ResponseEntity<CustomerRequestResponseDTO> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName().get()).isEqualTo("John Doe");
    }

    private void assertValidationError(ResponseEntity<String> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("must not be null");
    }

    private CustomerRequestStartDTO createInvalidCustomerRequestStartDTO() {
        AddressDTO addressDTO = new AddressDTO("Street 1", "2", "9499 CV", "City");
        CustomerRequestStartDTO customerRequestStartDTO = new CustomerRequestStartDTO();
        customerRequestStartDTO.setAddress(addressDTO); // Missing name and dateOfBirth
        return customerRequestStartDTO;
    }
}