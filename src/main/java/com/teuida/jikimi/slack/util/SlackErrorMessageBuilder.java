package com.teuida.jikimi.slack.util;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.teuida.jikimi.domain.exception.BusinessException;
import com.teuida.jikimi.domain.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;

public class SlackErrorMessageBuilder {

    private static final int MAX_MESSAGE_LENGTH = 150;

    private SlackErrorMessageBuilder() {
    }

    /**
     * 비즈니스 예외를 Slack 메시지 블록으로 변환
     */
    public static List<LayoutBlock> buildErrorBlocks(BusinessException exception) {
        List<LayoutBlock> blocks = new ArrayList<>();

        String message = truncateIfNeeded(exception.getUserMessage());
        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(":sadblob: *헉.. 처리하는 과정에서 에러가 발생했습니다*\n" + message)
                        .build())
                .build());

        return blocks;
    }

    /**
     * 검증 예외를 Slack 메시지 블록으로 변환 (필드별 오류 표시)
     */
    public static List<LayoutBlock> buildValidationErrorBlocks(ValidationException exception) {
        List<LayoutBlock> blocks = new ArrayList<>();

        StringBuilder sb = new StringBuilder(":sad: *입력값 검증 오류*\n\n");
        exception.getViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            sb.append(String.format("*%s*: %s\n", fieldName, message));
        });

        String message = truncateIfNeeded(sb.toString());
        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(message)
                        .build())
                .build());

        return blocks;
    }

    /**
     * 예상하지 못한 예외를 Slack 메시지 블록으로 변환
     */
    public static List<LayoutBlock> buildUnexpectedErrorBlocks(Exception exception) {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.add(SectionBlock.builder()
                .text(MarkdownTextObject.builder()
                        .text(":alarms: *예상하지 못한 오류가 발생했습니다*\n잠시 후 다시 시도해주세요...\n\n" + truncateIfNeeded(exception.getMessage()))
                        .build())
                .build());

        return blocks;
    }

    /**
     * 메시지가 너무 긴 경우 잘라내기
     */
    private static String truncateIfNeeded(String message) {
        if (message.length() > MAX_MESSAGE_LENGTH) {
            return message.substring(0, MAX_MESSAGE_LENGTH) + "...";
        }
        return message;
    }
}
