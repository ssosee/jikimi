package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IssueStatus {
    OPEN("열림"),
    IN_PROGRESS("진행 중"),
    CLOSED("닫힘");

    private final String displayName;
}