package com.teuida.jikimi.slack.exception;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.teuida.jikimi.domain.exception.BusinessException;
import com.teuida.jikimi.domain.exception.ValidationException;
import com.teuida.jikimi.slack.util.SlackErrorMessageBuilder;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackExceptionHandler {

    /**
     * 예외를 처리하고 Slack 사용자에게 에러 메시지 전송
     *
     * @param client    Slack MethodsClient
     * @param channelId 채널 ID
     * @param userId    사용자 ID (ephemeral 메시지 대상)
     * @param exception 발생한 예외
     */
    public void handleException(MethodsClient client, String channelId, String userId, Exception exception) {
        try {
            List<LayoutBlock> errorBlocks = buildErrorBlocks(exception);
            sendEphemeralMessage(client, channelId, userId, errorBlocks);
        } catch (Exception e) {
            log.error("Failed to send error message to Slack", e);
        }
    }

    /**
     * 예외 타입에 따라 적절한 에러 블록 생성
     */
    private List<LayoutBlock> buildErrorBlocks(Exception exception) {
        if (exception instanceof ValidationException validationException) {
            log.warn("Validation error occurred: {}", validationException.getUserMessage());
            return SlackErrorMessageBuilder.buildValidationErrorBlocks(validationException);

        } else if (exception instanceof BusinessException businessException) {
            log.warn("Business error occurred: {}", businessException.getUserMessage());
            return SlackErrorMessageBuilder.buildErrorBlocks(businessException);

        } else if (exception instanceof SlackApiException) {
            log.error("Slack API error occurred", exception);
            return SlackErrorMessageBuilder.buildUnexpectedErrorBlocks(exception);

        } else {
            log.error("Unexpected error occurred", exception);
            return SlackErrorMessageBuilder.buildUnexpectedErrorBlocks(exception);
        }
    }

    /**
     * Ephemeral 메시지 전송 (해당 사용자만 볼 수 있음)
     */
    private void sendEphemeralMessage(MethodsClient client, String channelId, String userId,
                                      List<LayoutBlock> blocks) throws SlackApiException, IOException {
        client.chatPostEphemeral(builder -> builder
                .channel(channelId)
                .user(userId)
                .blocks(blocks)
                .text("오류가 발생했습니다.") // fallback text
        );
    }
}
