package nl.co.geminibank.accountopening.boundary.exception;

import lombok.Getter;

@Getter
public class CustomerApplicationIsSubmittedException extends BaseCustomerException {

    public CustomerApplicationIsSubmittedException(String value, String errorMessage) {
        super(value, errorMessage);
    }
}
