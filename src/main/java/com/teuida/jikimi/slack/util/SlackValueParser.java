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
    public static String extractStringValue(Map<String, Map<String, Value>> values,
                                            String blockId, String actionId) {
        try {
            String value = values.get(blockId).get(actionId).getValue();
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(blockId + " 필드는 필수입니다");
            }
            return value;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(blockId + " 필드를 찾을 수 없습니다", e);
        }
    }

    public static String extractStringSelectedOption(Map<String, Map<String, Value>> values,
                                                     String blockId, String actionId) {
        try {
            return values.get(blockId).get(actionId).getSelectedOption().getValue();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(blockId + " 필드를 찾을 수 없습니다", e);
        }
    }

    /**
     * plainTextInput 또는 emailTextInput에서 Optional String 값 추출 (선택사항)
     */
    public static Optional<String> extractOptionalString(Map<String, Map<String, Value>> values,
                                                         String blockId, String actionId) {
        try {
            return Optional.ofNullable(values.get(blockId))
                    .map(block -> block.get(actionId))
                    .map(Value::getValue)
                    .filter(s -> !s.isBlank());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * staticSelect에서 Enum 값 추출
     */
    public static <E extends Enum<E>> E extractEnum(Map<String, Map<String, Value>> values,
                                                    String blockId, String actionId,
                                                    Class<E> enumClass) {
        try {
            String value = values.get(blockId)
                    .get(actionId)
                    .getSelectedOption()
                    .getValue();
            if (value == null) {
                throw new IllegalArgumentException(blockId + " 필드는 필수입니다");
            }
            return Enum.valueOf(enumClass, value);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(blockId + " 필드를 찾을 수 없습니다", e);
        }
    }

    /**
     * multiStaticSelect에서 Enum Set 추출
     */
    public static <E extends Enum<E>> Set<E> extractEnumSet(Map<String, Map<String, Value>> values,
                                                            String blockId, String actionId,
                                                            Class<E> enumClass) {
        try {
            Set<E> result = values.get(blockId)
                    .get(actionId)
                    .getSelectedOptions()
                    .stream()
                    .map(opt -> Enum.valueOf(enumClass, opt.getValue()))
                    .collect(Collectors.toSet());

            if (result.isEmpty()) {
                throw new IllegalArgumentException(blockId + " 필드에서 최소 1개를 선택해야 합니다");
            }
            return result;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(blockId + " 필드를 찾을 수 없습니다", e);
        }
    }

    /**
     * multiStaticSelect에서 String Set 추출
     */
    public static Set<String> extractStringSet(Map<String, Map<String, Value>> values,
                                               String blockId, String actionId) {
        try {
            Set<String> result = values.get(blockId)
                    .get(actionId)
                    .getSelectedOptions()
                    .stream()
                    .map(SelectedOption::getValue)
                    .collect(Collectors.toSet());

            if (result.isEmpty()) {
                throw new IllegalArgumentException(blockId + " 필드에서 최소 1개를 선택해야 합니다");
            }
            return result;
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(blockId + " 필드를 찾을 수 없습니다", e);
        }
    }

    /**
     * multiStaticSelect에서 Optional Enum Set 추출 (선택사항)
     */
    public static <E extends Enum<E>> Set<E> extractOptionalEnumSet(Map<String, Map<String, Value>> values,
                                                                    String blockId, String actionId,
                                                                    Class<E> enumClass) {
        try {
            return Optional.ofNullable(values.get(blockId))
                    .map(block -> block.get(actionId))
                    .map(Value::getSelectedOptions)
                    .map(options -> options.stream()
                            .map(opt -> Enum.valueOf(enumClass, opt.getValue()))
                            .collect(Collectors.toSet()))
                    .orElse(Set.of());
        } catch (Exception e) {
            return Set.of();
        }
    }
}
