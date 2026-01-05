package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActionType {
    CREATED("생성"),
    DELETED("삭제"),
    ASSIGNED("할당"),
    REASSIGNED("재할당"),
    JIRA_CREATED("JIRA 티켓 생성"),
    RESOLVE("해결");

    private final String displayName;
}
