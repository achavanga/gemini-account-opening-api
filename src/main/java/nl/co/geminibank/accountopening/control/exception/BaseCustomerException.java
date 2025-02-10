package nl.co.geminibank.accountopening.control.exception;

import lombok.Getter;

@Getter
public abstract class BaseCustomerException extends RuntimeException {

    private final String value;
    private final String errorMessage;

    public BaseCustomerException(String value, String errorMessage) {
        super(errorMessage);
        this.value = value;
        this.errorMessage = errorMessage;
    }
}