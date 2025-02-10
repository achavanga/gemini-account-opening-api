package nl.co.geminibank.accountopening.control.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {
    private static final int ADULT_AGE = 18;

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        return dateOfBirth != null && ChronoUnit.YEARS.between(dateOfBirth, LocalDate.now()) >= ADULT_AGE;
    }
}
