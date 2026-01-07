package com.teuida.jikimi.slack.service;

import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

import com.slack.api.model.block.composition.OptionObject;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract public class SlackModalBuilder {
    public static final String MODAL = "modal";
    public static final String HOME = "home";
    public static final String WORKFLOW_STEP = "workflow_step";
    public static final String PLAIN_TEXT = "plain_text";

    public static <E extends Enum<E>> List<OptionObject> createOptions(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(value -> option(plainText(value.toString()), value.toString()))
                .collect(Collectors.toList());
    }
}
