package com.teuida.jikimi.domain.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(Class<?> clazz) {
        super(String.format("%s을(를) 찾을 수 없습니다.", clazz.getSimpleName()));
    }
}
