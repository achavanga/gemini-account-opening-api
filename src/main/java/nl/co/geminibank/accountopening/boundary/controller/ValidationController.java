package nl.co.geminibank.accountopening.boundary.controller;


import nl.co.geminibank.accountopening.api.ValidationApi;
import nl.co.geminibank.accountopening.control.exception.ValidationException;
import nl.co.geminibank.accountopening.control.service.ValidationService;
import nl.co.geminibank.accountopening.model.ApiCustomersValidatePostRequestDTO;
import nl.co.geminibank.accountopening.model.ValidationSuccessResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static nl.co.geminibank.accountopening.util.Constants.API_PATH_VERSON;

/**
 * REST controller for handling customer validation requests.
 * Implements the {@link ValidationApi} interface.
 */
@RestController
@RequestMapping(API_PATH_VERSON)
public class ValidationController implements ValidationApi {

    private final ValidationService validationService;

    /**
     * Constructs a new {@link ValidationController} with the given {@link ValidationService}.
     *
     * @param validationService The validation service to be used for field validation.
     */
    public ValidationController(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Frontend Validates the provided field and value for customer records.
     * If both the field and value are present, the field is validated, and a success response is returned.
     * If any of the field or value is missing, a {@link ValidationException} is thrown.
     *
     * @param apiCustomersValidatePostRequestDTO The request body containing the field and value to validate.
     * @return A {@link ResponseEntity} containing a {@link ValidationSuccessResponseDTO} if validation is successful.
     * @throws ValidationException if the field or value is invalid or missing.
     */
    @Override
    public ResponseEntity<ValidationSuccessResponseDTO> apiCustomersValidatePost(ApiCustomersValidatePostRequestDTO apiCustomersValidatePostRequestDTO) {
        var fieldName = apiCustomersValidatePostRequestDTO.getField();
        var fieldValue = apiCustomersValidatePostRequestDTO.getValue();

        // Ensure both field and value are present
        var responseDTO = fieldName
                .map(field -> fieldValue
                        .map(value -> {
                            validationService.validateField(field, value);
                            var successResponse = new ValidationSuccessResponseDTO();
                            successResponse.setValid(true);
                            successResponse.setMessage(String.format("The %s is valid.", field));
                            return successResponse;
                        })
                        .orElseThrow(() -> new ValidationException(field, "Value is required for field: " + field)))
                .orElseThrow(() -> new ValidationException("", "Field is required"));

        return ResponseEntity.ok(responseDTO);
    }
}
