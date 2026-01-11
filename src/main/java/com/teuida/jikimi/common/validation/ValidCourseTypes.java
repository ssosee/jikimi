package com.teuida.jikimi.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CourseTypesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCourseTypes {

    String message() default "언어 코드에서 ALL 타입은 단독으로만 선택할 수 있습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
