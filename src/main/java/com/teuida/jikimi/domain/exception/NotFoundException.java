package com.teuida.jikimi.domain.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Class<?> clazz) {
        super(String.format("%s Not Found", clazz.getSimpleName()));
    }
}
