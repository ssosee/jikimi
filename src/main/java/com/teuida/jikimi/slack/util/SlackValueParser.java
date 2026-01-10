package com.teuida.jikimi.slack.util;

import com.slack.api.model.view.ViewState.SelectedOption;
import com.slack.api.model.view.ViewState.Value;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SlackValueParser {

    private SlackValueParser() {
    }  // 인스턴스화 방지

    /**
     * plainTextInput 또는 emailTextInput에서 String 값 추출
     */
    public static String extractString(Map<String, Map<String, Value>> values,
                                       String blockId, String actionId) {
        return values.get(blockId).get(actionId).getValue();
    }

    /**
     * plainTextInput 또는 emailTextInput에서 Optional String 값 추출 (선택사항)
     */
    public static Optional<String> extractOptionalString(Map<String, Map<String, Value>> values,
                                                         String blockId, String actionId) {
        return Optional.ofNullable(values.get(blockId))
                .map(block -> block.get(actionId))
                .map(Value::getValue)
                .filter(s -> !s.isBlank());
    }

    /**
     * staticSelect에서 Enum 값 추출
     */
    public static <E extends Enum<E>> E extractEnum(Map<String, Map<String, Value>> values,
                                                    String blockId, String actionId,
                                                    Class<E> enumClass) {
        String value = values.get(blockId)
                .get(actionId)
                .getSelectedOption()
                .getValue();
        return Enum.valueOf(enumClass, value);
    }

    /**
     * multiStaticSelect에서 Enum Set 추출
     */
    public static <E extends Enum<E>> Set<E> extractEnumSet(Map<String, Map<String, Value>> values,
                                                            String blockId, String actionId,
                                                            Class<E> enumClass) {
        return values.get(blockId)
                .get(actionId)
                .getSelectedOptions()
                .stream()
                .map(opt -> Enum.valueOf(enumClass, opt.getValue()))
                .collect(Collectors.toSet());
    }

    /**
     * multiStaticSelect에서 String Set 추출
     */
    public static Set<String> extractStringSet(Map<String, Map<String, Value>> values,
                                               String blockId, String actionId) {
        return values.get(blockId)
                .get(actionId)
                .getSelectedOptions()
                .stream()
                .map(SelectedOption::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * multiStaticSelect에서 Optional Enum Set 추출 (선택사항)
     */
    public static <E extends Enum<E>> Set<E> extractOptionalEnumSet(Map<String, Map<String, Value>> values,
                                                                    String blockId, String actionId,
                                                                    Class<E> enumClass) {
        return Optional.ofNullable(values.get(blockId))
                .map(block -> block.get(actionId))
                .map(Value::getSelectedOptions)
                .map(options -> options.stream()
                        .map(opt -> Enum.valueOf(enumClass, opt.getValue()))
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }
}
