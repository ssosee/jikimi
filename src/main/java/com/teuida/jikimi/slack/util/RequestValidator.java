package com.teuida.jikimi.slack.util;

import com.teuida.jikimi.domain.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private final Validator validator;

    /**
     * 객체 검증 및 예외 발생
     *
     * @param object 검증할 객체
     * @param <T> 객체 타입
     * @throws ValidationException 검증 실패 시
     */
    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            throw new ValidationException((Set) violations);
        }
    }

    /**
     * 객체 검증 및 검증 결과 반환
     *
     * @param object 검증할 객체
     * @param <T> 객체 타입
     * @return 검증 위반 항목 Set (비어있으면 검증 성공)
     */
    public <T> Set<ConstraintViolation<T>> validateAndReturn(T object) {
        return validator.validate(object);
    }
}
