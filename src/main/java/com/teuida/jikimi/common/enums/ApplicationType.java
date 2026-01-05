package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    IOS(":ios: iOS"),
    ANDROID(":android: Android"),
    SERVER(":java: Server"),
    WEB(":react: Web");

    private final String displayName;
}
