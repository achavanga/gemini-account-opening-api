package nl.co.geminibank.accountopening.control.service;

import jakarta.validation.Validator;
import nl.co.geminibank.accountopening.control.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private Validator validator;

    @InjectMocks
    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService(validator);
    }

    @Test
    void shouldValidateValidFieldSuccessfully() {
        String field = "email";
        String value = "test@example.com";
        when(validator.validateProperty(any(), eq(field))).thenReturn(Collections.emptySet());

        assertThatCode(() -> validationService.validateField(field, value))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowValidationExceptionWhenFieldIsInvalid() {
        String field = "invalidField";
        String value = "someValue";

        assertThatThrownBy(() -> validationService.validateField(field, value))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid field: " + field);
    }

    @Test
    void shouldConvertAndValidateBooleanField() {
        String field = "interestedInOtherProducts";
        String value = "true";
        when(validator.validateProperty(any(), eq(field))).thenReturn(Collections.emptySet());

        assertThatCode(() -> validationService.validateField(field, value))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldConvertAndValidateLocalDateField() {
        String field = "dateOfBirth";
        String value = "2000-01-01";
        when(validator.validateProperty(any(), eq(field))).thenReturn(Collections.emptySet());

        assertThatCode(() -> validationService.validateField(field, value))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForInvalidIntegerConversion() {
        String field = "age";
        String value = "notANumber";

        assertThatThrownBy(() -> validationService.validateField(field, value))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidBooleanConversion() {
        String field = "interestedInOtherProducts";
        String value = "notABoolean";

        assertThatCode(() -> validationService.validateField(field, value))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowExceptionForInvalidDateConversion() {
        String field = "birthdate";
        String value = "notADate";

        assertThatThrownBy(() -> validationService.validateField(field, value))
                .isInstanceOf(Exception.class);
    }
}