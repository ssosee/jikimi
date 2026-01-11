package com.teuida.jikimi.slack.aspect;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload;
import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload;
import com.slack.api.app_backend.views.payload.ViewSubmissionPayload;
import com.slack.api.bolt.context.Context;
import com.slack.api.bolt.request.Request;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.methods.MethodsClient;
import com.teuida.jikimi.slack.exception.SlackExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)  // Execute before other aspects
@RequiredArgsConstructor
public class SlackHandlerExceptionAspect {

    private final SlackExceptionHandler slackExceptionHandler;

    /**
     * Slack 핸들러 서비스 메서드를 가로채서 예외 처리 패턴: IssueHandlerService의 모든 public 메서드
     */
    @Around("execution(public * com.teuida.jikimi.slack.issue.service.IssueHandlerService.*(..))")
    public Object handleSlackHandlerException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            // 정상 실행
            return joinPoint.proceed();

        } catch (Exception exception) {
            log.error("Exception in Slack handler: {}", joinPoint.getSignature().getName(), exception);

            // Slack Context 및 Request 추출
            SlackContextInfo contextInfo = extractSlackContext(joinPoint);

            if (contextInfo != null) {
                try {
                    // 사용자에게 에러 메시지 전송
                    slackExceptionHandler.handleException(
                            contextInfo.client(),
                            contextInfo.channelId(),
                            contextInfo.userId(),
                            exception
                    );

                    // Slack에 ack 응답 (오류가 발생해도 요청은 처리되었음을 알림)
                    return contextInfo.context().ack();

                } catch (Exception sendException) {
                    // 에러 메시지 전송 실패 시에도 로그만 남기고 ack 응답
                    log.error("Failed to send error message to Slack", sendException);
                    return contextInfo.context().ack();
                }
            } else {
                // Context 추출 실패 시 원본 예외를 다시 던짐
                log.error("Could not extract Slack context from handler arguments");
                throw exception;
            }
        }
    }

    /**
     * 핸들러 메서드 인자에서 Slack Context 정보 추출
     */
    private SlackContextInfo extractSlackContext(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        Request<?> request = null;
        Context context = null;

        // Request와 Context 객체 찾기
        for (Object arg : args) {
            if (arg instanceof Request<?>) {
                request = (Request<?>) arg;
            } else if (arg instanceof Context) {
                context = (Context) arg;
            }
        }

        if (request == null || context == null) {
            return null;
        }

        // 채널 ID 추출 (request 타입에 따라 다름)
        String channelId = extractChannelId(request);
        String userId = context.getRequestUserId();
        MethodsClient client = context.client();

        if (channelId == null || userId == null || client == null) {
            return null;
        }

        return new SlackContextInfo(client, channelId, userId, context);
    }

    /**
     * Request 타입에 따라 채널 ID 추출
     */
    private String extractChannelId(Request<?> request) {
        // ViewSubmissionRequest의 경우
        if (request instanceof ViewSubmissionRequest viewRequest) {
            ViewSubmissionPayload viewPayload = viewRequest.getPayload();
            // privateMetadata에서 채널 ID 추출 (CreateIssueRequest 패턴)
            String privateMetadata = viewPayload.getView().getPrivateMetadata();
            if (privateMetadata != null && !privateMetadata.isBlank()) {
                return privateMetadata;
            }
            // privateMetadata가 없는 경우 user DM 채널 사용
            return viewPayload.getUser() != null ? viewPayload.getUser().getId() : null;
        }

        // BlockActionRequest의 경우
        if (request instanceof BlockActionRequest blockRequest) {
            BlockActionPayload blockPayload = blockRequest.getPayload();
            return blockPayload.getChannel() != null ? blockPayload.getChannel().getId() : null;
        }

        // SlashCommandRequest의 경우
        if (request instanceof SlashCommandRequest slashRequest) {
            SlashCommandPayload slashPayload = slashRequest.getPayload();
            return slashPayload.getChannelId();
        }

        return null;
    }

    /**
     * Slack Context 정보를 담는 레코드
     */
    private record SlackContextInfo(MethodsClient client, String channelId, String userId, Context context) {
    }
}
