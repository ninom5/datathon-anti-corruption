package com.datathon.api.common;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OibValidator implements ConstraintValidator<Oib, String> {

    private static final int LENGTH = 11;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || isValidOib(value);
    }

    public static boolean isValidOib(String value) {
        if (value.length() != LENGTH || !value.chars().allMatch(character -> character >= '0' && character <= '9')) {
            return false;
        }
        return checkDigit(value) == value.charAt(LENGTH - 1) - '0';
    }

    /** ISO 7064, MOD 11,10 over the first ten digits. */
    private static int checkDigit(String oib) {
        int remainder = 10;
        for (int index = 0; index < LENGTH - 1; index++) {
            remainder = (remainder + oib.charAt(index) - '0') % 10;
            if (remainder == 0) {
                remainder = 10;
            }
            remainder = (remainder * 2) % 11;
        }
        int digit = 11 - remainder;
        return digit == 10 ? 0 : digit;
    }
}
