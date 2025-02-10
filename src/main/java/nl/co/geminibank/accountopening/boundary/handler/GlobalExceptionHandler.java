package nl.co.geminibank.accountopening.boundary.handler;

import jakarta.validation.ConstraintViolationException;
import nl.co.geminibank.accountopening.control.exception.CustomerApplicationIsSubmittedException;
import nl.co.geminibank.accountopening.control.exception.CustomerException;
import nl.co.geminibank.accountopening.control.exception.CustomerNotFoundException;
import nl.co.geminibank.accountopening.control.exception.ValidationException;
import nl.co.geminibank.accountopening.model.ErrorResponseDTO;
import nl.co.geminibank.accountopening.model.ValidationFailureResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for managing different types of exceptions in the application.
 * This class handles validation exceptions, constraint violations, and specific customer-related exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ValidationException and returns a response with the validation failure details.
     *
     * @param ex the ValidationException to handle
     * @return ResponseEntity with the validation failure details
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationFailureResponseDTO> handleValidationException(ValidationException ex) {
        return buildValidationErrorResponse(ex.getField(), ex.getErrorMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ConstraintViolationException and returns a response with the list of validation errors.
     *
     * @param ex the ConstraintViolationException to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the list of validation errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        List<ValidationFailureResponseDTO> validationErrors = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationFailureResponseDTO(false,
                        violation.getMessage(), violation.getPropertyPath().toString()))
                .collect(Collectors.toList());

        return buildErrorResponse(validationErrors, request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    /**
     * Handles MethodArgumentNotValidException and returns a response with the list of validation errors.
     *
     * @param ex the MethodArgumentNotValidException to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the list of validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {
        List<ValidationFailureResponseDTO> validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ValidationFailureResponseDTO(false,
                        fieldError.getDefaultMessage(), fieldError.getField()))
                .collect(Collectors.toList());

        return buildErrorResponse(validationErrors, request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    /**
     * Handles CustomerApplicationIsSubmittedException and returns a response with the error details.
     *
     * @param ex the CustomerApplicationIsSubmittedException to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the error details
     */
    @ExceptionHandler(CustomerApplicationIsSubmittedException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerApplicationIsSubmittedException(CustomerApplicationIsSubmittedException ex, WebRequest request) {
        return buildErrorResponse(List.of(new ValidationFailureResponseDTO(false, ex.getErrorMessage(),
                ex.getValue())), request, HttpStatus.FORBIDDEN, "FORBIDDEN_ERROR");
    }

    /**
     * Handles CustomerNotFoundException and returns a response with the error details.
     *
     * @param ex the CustomerNotFoundException to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the error details
     */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerNotFoundException(CustomerNotFoundException ex, WebRequest request) {
        return buildErrorResponse(List.of(new ValidationFailureResponseDTO(false, ex.getErrorMessage(),
                ex.getValue())), request, HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR");
    }

    /**
     * Handles CustomerException and returns a response with the error details.
     *
     * @param ex the CustomerException to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the error details
     */
    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerException(CustomerException ex, WebRequest request) {
        return buildErrorResponse(List.of(new ValidationFailureResponseDTO(false, ex.getErrorMessage(),
                ex.getValue())), request, HttpStatus.BAD_REQUEST, "VALIDATION_ERROR");
    }

    /**
     * Handles general exceptions and returns a response with the error details.
     *
     * @param ex the Exception to handle
     * @param request the WebRequest containing request details
     * @return ResponseEntity with the error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(List.of(new ValidationFailureResponseDTO(false, ex.getMessage(),
                ex.toString())), request, HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR");
    }

    /**
     * Builds a validation error response with the given field, error message, and HTTP status.
     *
     * @param field the field associated with the validation error
     * @param errorMessage the error message
     * @param status the HTTP status code
     * @return ResponseEntity with the validation error details
     */
    private ResponseEntity<ValidationFailureResponseDTO> buildValidationErrorResponse(String field, String errorMessage, HttpStatus status) {
        ValidationFailureResponseDTO response = new ValidationFailureResponseDTO(false, errorMessage, field);
        return new ResponseEntity<>(response, status);
    }

    /**
     * Builds an error response with the given validation errors, request details, HTTP status, and error code.
     *
     * @param validationErrors the list of validation errors
     * @param request the WebRequest containing request details
     * @param status the HTTP status code
     * @param errorCode the error code associated with the response
     * @return ResponseEntity with the error details
     */
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(List<ValidationFailureResponseDTO> validationErrors,
                                                                WebRequest request, HttpStatus status, String errorCode) {
        ErrorResponseDTO errorResponse = createErrorResponse(validationErrors, request, status, errorCode);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Creates an ErrorResponseDTO with the given validation errors, request details, HTTP status, and error code.
     *
     * @param validationErrors the list of validation errors
     * @param request the WebRequest containing request details
     * @param status the HTTP status code
     * @param errorCode the error code associated with the response
     * @return ErrorResponseDTO with the error details
     */
    private ErrorResponseDTO createErrorResponse(List<ValidationFailureResponseDTO> validationErrors,
                                                 WebRequest request, HttpStatus status, String errorCode) {
        return new ErrorResponseDTO(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                errorCode,
                validationErrors,
                request.getDescription(false).replace("uri=", "") // Extract API path
        );
    }
}
