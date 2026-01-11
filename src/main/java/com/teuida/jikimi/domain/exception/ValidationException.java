package com.teuida.jikimi.domain.exception;

import jakarta.validation.ConstraintViolation;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidationException extends BusinessException {
    private final Set<ConstraintViolation<?>> violations;

    public ValidationException(Set<ConstraintViolation<?>> violations) {
        super(buildMessage(violations));
        this.violations = violations;
    }

    private static String buildMessage(Set<ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("\n"));
    }

    public Set<ConstraintViolation<?>> getViolations() {
        return violations;
    }
}
