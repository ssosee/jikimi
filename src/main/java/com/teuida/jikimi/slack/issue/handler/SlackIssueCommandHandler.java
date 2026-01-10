package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.COMMAND_ISSUE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ISSUE_MODAL;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Usergroup;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.IssueModalBuilder;
import com.teuida.jikimi.slack.service.SlackService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueCommandHandler implements SlackHandlerRegistrar {

    private final SlackService slackService;

    @Override
    public void register(App app) {
        app.command(COMMAND_ISSUE, this::issueCommandHandler);
    }

    public Response issueCommandHandler(SlashCommandRequest req, SlashCommandContext ctx) throws SlackApiException, IOException {

        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        String channelId = req.getPayload().getChannelId();

        // 슬랙 팀 정보 조회
        List<Usergroup> findUsergroups = slackService.fetchUserGroups(client, botToken);

        // 모달 생성
        client.viewsOpen(builder -> builder
                .triggerId(ctx.getTriggerId())
                .view(IssueModalBuilder.buildIssueModal(
                        channelId, ctx.getBotId(),
                        findUsergroups, ISSUE_MODAL))
        );

        return ctx.ack();
    }
}
