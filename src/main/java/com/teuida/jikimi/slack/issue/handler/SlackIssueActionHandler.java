package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueActionHandler implements SlackHandlerRegistrar {

    private final IssueService issueService;

    @Override
    public void register(App app) {
        app.blockAction(ACTION_ASSIGN_TO_ME, this::handleAssignToMe);
        app.blockAction(ACTION_SELECT_ASSIGNEE, this::handleSelectAssignee);
    }

    public Response handleAssignToMe(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String blockId = req.getPayload().getActions().getFirst().getBlockId();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // 이슈 할당
        Issue issue = issueService.assign(Long.parseLong(blockId), requestUserId);

        // 슬랙 메시지 원본 수정
        client.chatUpdate(builder -> builder
                .channel(issue.slackChannelId())
                .ts(issue.slackMessageTs())
                .blocks(IssueBlockBuilder.buildIssueBlocks(issue))
        );

        // 해당 스레드에 이슈 할당 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(issue.slackChannelId())
                .token(ctx.getBotToken())
                .threadTs(issue.slackMessageTs())
                .text(String.format("🙌 <@%s>님이 본인에게 이슈를 할당 했습니다.", requestUserId))
        );

        // 해당 스레드에 이모지 추가
        client.reactionsAdd(builder -> builder
                .token(ctx.getBotToken())
                .channel(issue.slackChannelId())
                .timestamp(issue.slackMessageTs())
                .name(":blue_loading:")
        );

        return ctx.ack();
    }

    public Response handleSelectAssignee(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        Action action = req.getPayload().getActions().getFirst();

        String blockId = action.getBlockId();
        String selectedUserId = action.getSelectedUser();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // 이슈 할당
        Issue issue = issueService.assign(Long.parseLong(blockId), selectedUserId);

        // 슬랙 메시지 원본 ️
        client.chatUpdate(builder -> builder
                .channel(issue.slackChannelId())
                .ts(issue.slackMessageTs())
                .blocks(IssueBlockBuilder.buildIssueBlocks(issue))
        );

        // 해당 스레드에 이슈 할당 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(issue.slackChannelId())
                .token(ctx.getBotToken())
                .threadTs(issue.slackMessageTs())
                .text(String.format("🕊️ <@%s>님이 <@%s>에게 이슈를 할당 했습니다.", requestUserId, selectedUserId))
        );

        // 해당 스레드에 이모지 추가
        client.reactionsAdd(builder -> builder
                .token(ctx.getBotToken())
                .channel(issue.slackChannelId())
                .timestamp(issue.slackMessageTs())
                .name(":blue_loading:")
        );

        return ctx.ack();
    }
}
