package nl.co.geminibank.accountopening.control.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AdultValidator.class)
public @interface Adult {
    String message() default "Customer must be at least 18 years old.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

