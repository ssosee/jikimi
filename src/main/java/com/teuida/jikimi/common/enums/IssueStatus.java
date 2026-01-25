package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IssueStatus {
    OPEN("접수"),
    IN_PROGRESS("진행중"),
    CLOSED("완료");

    private final String displayName;
}