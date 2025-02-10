package nl.co.geminibank.accountopening.boundary.exception;

import lombok.Getter;

@Getter
public class CustomerNotFoundException extends BaseCustomerException {
    public CustomerNotFoundException(String value, String errorMessage) {
        super(value, errorMessage);
    }
}