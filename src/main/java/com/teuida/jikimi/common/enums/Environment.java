package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Environment {
    ALL("ALL"),
    PROD("PROD"),
    DEV("DEV");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
}
