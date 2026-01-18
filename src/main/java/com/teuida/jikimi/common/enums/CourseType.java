package com.teuida.jikimi.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourseType {
    ALL("🌎 All", "All"),
    KOEN("🇰🇷 KOEN", "KOEN"),
    JANE("🇯🇵 JAEN", "JAEN"),
    ESEN("🇪🇸 ESEN", "ESEN"),
    KOJA("🇰🇷 KOJA", "KOJA"),
    FREN("🇫🇷 FREN", "FREN"),
    ZHEN("🇨🇳 ZHEN", "ZHEN");

    private final String displayNameForSlack;
    private final String displayNameForJira;

    @Override
    public String toString() {
        return displayNameForSlack;
    }
}
