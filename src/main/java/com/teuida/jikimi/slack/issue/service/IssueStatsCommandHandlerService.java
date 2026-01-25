package com.teuida.jikimi.slack.issue.service;

import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.parser.IssueStatsParameterParser;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueStatsCommandHandlerService {
    private final StatsAsyncExecutor statsAsyncExecutor;

    @Value("${slack.workspace-url}")
    private String workspaceUrl;

    public Response handleCommandIssueStats(SlashCommandRequest req, SlashCommandContext ctx)
            throws SlackApiException, IOException {

        // 1. 파라미터 파싱
        String commandText = req.getPayload().getText();
        MethodsClient client = ctx.client();
        String channelId = req.getPayload().getChannelId();
        String userId = ctx.getRequestUserId();
        IssueStatsFilter filter = IssueStatsParameterParser.parse(commandText);

        // 즉시 응답 (3초 내) - ephemeral 메시지
        client.chatPostEphemeral(builder -> builder
                .channel(channelId)
                .user(userId)
                .text(":hourglass_flowing_sand: 이슈 현황 생성중...")
        );

        statsAsyncExecutor.execute(client, channelId, userId, filter, commandText, workspaceUrl);

        return ctx.ack();
    }

    public record IssueStatsButtonValue(List<Long> issueIds, String commandText) {
    }
}
