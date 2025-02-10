package nl.co.geminibank.accountopening.control.exception;

import lombok.Getter;

@Getter
public class CustomerException extends BaseCustomerException {
    public CustomerException(String value, String errorMessage) {
        super(value, errorMessage);
    }
}
