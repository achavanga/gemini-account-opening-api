package nl.co.geminibank.accountopening.boundary.controller;


import lombok.RequiredArgsConstructor;
import nl.co.geminibank.accountopening.api.AccountOpeningApi;
import nl.co.geminibank.accountopening.control.service.AccountOpeningService;
import nl.co.geminibank.accountopening.model.CustomerRequestResponseDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestStartDTO;
import nl.co.geminibank.accountopening.model.CustomerRequestUpdateDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static nl.co.geminibank.accountopening.util.Constants.API_PATH_VERSON;

/**
 * REST controller for handling account opening requests.
 * Implements the {@link AccountOpeningApi} interface.
 */
@RestController
@RequestMapping(API_PATH_VERSON)
@RequiredArgsConstructor
public class AccountOpeningController implements AccountOpeningApi {
    private final AccountOpeningService accountOpeningService;

    /**
     * Retrieves the customer account registration by request ID.
     *
     * @param requestId the unique identifier for the customer request.
     * @return the {@link CustomerRequestResponseDTO} containing the customer details.
     */
    @Override
    public ResponseEntity<CustomerRequestResponseDTO> apiCustomersRequestIdGet(String requestId) {
        return buildResponse(accountOpeningService.getCustomerAccountRegistrationById(requestId), HttpStatus.OK);
    }

    /**
     * Starts the account registration process for the customer.
     *
     * @param customerRequestStartDTO the request DTO containing customer details for account opening.
     * @return the {@link CustomerRequestResponseDTO} containing the registration response.
     */
    @Override
    public ResponseEntity<CustomerRequestResponseDTO> apiCustomersStartPost(CustomerRequestStartDTO customerRequestStartDTO) {
        return buildResponse(accountOpeningService.startAccountRegistration(customerRequestStartDTO), HttpStatus.CREATED);
    }

    /**
     * Pauses the account registration process for the customer.
     *
     * @param requestId the unique identifier for the customer request.
     * @return the {@link CustomerRequestResponseDTO} containing the pause response.
     */
    @Override
    public ResponseEntity<CustomerRequestResponseDTO> apiCustomersRequestIdPausePut(String requestId) {
        return buildResponse(accountOpeningService.pauseAccountRegistration(requestId), HttpStatus.ACCEPTED);
    }

    /**
     * Resumes the account registration process for the customer.
     *
     * @param requestId               the unique identifier for the customer request.
     * @param customerRequestStartDTO the DTO containing updated customer details for account registration.
     * @return the {@link CustomerRequestUpdateDTO} containing the updated registration response.
     */
    @Override
    public ResponseEntity<CustomerRequestUpdateDTO> apiCustomersRequestIdResumePut(String requestId, CustomerRequestStartDTO customerRequestStartDTO) {
        return buildResponse(accountOpeningService.resumeAccountRegistration(requestId, customerRequestStartDTO), HttpStatus.ACCEPTED);
    }

    /**
     * Helper method to construct a standard ResponseEntity.
     *
     * @param body   the body of the response.
     * @param status the HTTP status.
     * @param <T>    the type of the response body.
     * @return a {@link ResponseEntity} with the provided body and status.
     */
    private <T> ResponseEntity<T> buildResponse(T body, HttpStatus status) {
        return ResponseEntity.status(status).body(body);
    }
}
