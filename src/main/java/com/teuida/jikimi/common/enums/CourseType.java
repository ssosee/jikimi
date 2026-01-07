package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseType {
    ALL("🌎All"),
    KOEN("🇰🇷KOEN"),
    JANE("🇯🇵JAEN"),
    ESEN("🇪🇸ESEN"),
    KOJA("🇰🇷KOJA"),
    FREN("🇫🇷FREN"),
    ZHEN("🇨🇳ZHEN");

    private final String displayName;

    @Override
    public String toString() {
        return displayName;
    }
}
