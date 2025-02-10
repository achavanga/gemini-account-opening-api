package nl.co.geminibank.accountopening.boundary.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String field;
    private final String errorMessage;


    public ValidationException(String field, String errorMessage) {
        super(errorMessage);
        this.field = field;
        this.errorMessage = errorMessage;
    }
}

