package com.teuida.jikimi.common.annotation;

import com.teuida.jikimi.common.enums.ActionType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IssueLogging {
    ActionType actionType();
}
