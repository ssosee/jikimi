package com.teuida.jikimi.domain.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private final String userMessage;  // 사용자에게 표시할 메시지

    protected BusinessException(String userMessage) {
        super(userMessage);
        this.userMessage = userMessage;
    }

    protected BusinessException(String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.userMessage = userMessage;
    }
}
