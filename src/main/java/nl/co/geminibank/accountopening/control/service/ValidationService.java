package nl.co.geminibank.accountopening.control.service;

import jakarta.validation.Validator;
import nl.co.geminibank.accountopening.control.exception.ValidationException;
import nl.co.geminibank.accountopening.entity.model.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * Service responsible for validating fields in a Customer object.
 */
@Service
public class ValidationService {
    private static final Logger log = LoggerFactory.getLogger(ExpirePausedRequestService.class);
    private final Validator validator;

    public ValidationService(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validates a field of a customer by checking if the value is valid according to constraints.
     *
     * @param field The field name to be validated.
     * @param value The value of the field to be validated.
     * @throws ValidationException if the field or value is invalid.
     */
    public void validateField(String field, String value) {
        log.debug("Validating field {} with value {}", field, value);
        var customer = new Customer();
        var declaredField = getDeclaredField(field);

        // Convert value to the correct type and set it on the customer object
        var convertedValue = convertToCorrectType(declaredField.getType(), value);
        setFieldValue(declaredField, customer, convertedValue);

        var violations = validator.validateProperty(customer, field);
        if (!violations.isEmpty()) {
            throw new ValidationException(field, violations.iterator().next().getMessage());
        }
    }

    /**
     * Retrieves a declared field from the Customer class.
     *
     * @param field The field name.
     * @return The declared field.
     * @throws ValidationException if the field is not found.
     */
    private Field getDeclaredField(String field) {
        try {
            var declaredField = Customer.class.getDeclaredField(field);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (NoSuchFieldException e) {
            throw new ValidationException(field, "Invalid field: " + field);
        }
    }

    /**
     * Converts the given value to the appropriate type for the field.
     *
     * @param type The type of the field.
     * @param value The value to be converted.
     * @return The converted value.
     */
    private Object convertToCorrectType(Class<?> type, String value) {
        if (type.equals(Integer.class) || type.equals(int.class)) {
            return Integer.parseInt(value);
        } else if (type.equals(Double.class) || type.equals(double.class)) {
            return Double.parseDouble(value);
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return Boolean.parseBoolean(value);
        } else if (type.equals(LocalDate.class)) {
            return LocalDate.parse(value);
        }
        return value;
    }

    /**
     * Sets the value on the given field in the customer object.
     *
     * @param field The field to set.
     * @param customer The customer object.
     * @param value The value to set.
     * @throws ValidationException if the field cannot be accessed or set.
     */
    private void setFieldValue(Field field, Customer customer, Object value) {
        try {
            field.set(customer, value);
        } catch (IllegalAccessException e) {
            throw new ValidationException(field.getName(), "Unable to set value for field: " + field.getName());
        }
    }
}