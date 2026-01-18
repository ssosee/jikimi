package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationType {
    IOS(":ios: iOS", "iOS"),
    ANDROID(":android: Android", "Android"),
    SERVER(":java: Server", "Server"),
    WEB(":react: Web", "Web");

    private final String displayNameForSlack;
    private final String displayNameForJira;

    @Override
    public String toString() {
        return displayNameForSlack;
    }
}
