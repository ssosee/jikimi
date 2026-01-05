package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Environment {
    ALL("모든 환경"),
    PROD("운영"),
    DEV("개발");

    private final String displayName;
}
