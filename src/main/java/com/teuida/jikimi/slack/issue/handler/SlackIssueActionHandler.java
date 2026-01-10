package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_MORE_OPTIONS;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ISSUE_DELETE_CONFIRM_MODAL;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_DELETE_ISSUE;

import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action;
import com.slack.api.app_backend.interactive_components.payload.BlockActionPayload.Action.SelectedOption;
import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.AssignIssueRequest;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import com.teuida.jikimi.slack.issue.IssueModalBuilder;
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
        app.blockAction(ACTION_MORE_OPTIONS, this::handleMoreOptions);
    }

    public Response handleAssignToMe(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String blockId = req.getPayload().getActions().getFirst().getBlockId();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // request 생성
        AssignIssueRequest assignIssueRequest = AssignIssueRequest.create(requestUserId, Long.parseLong(blockId), requestUserId);

        // 이슈 할당
        Issue issue = issueService.assign(assignIssueRequest);

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
                .name("blue_loading")
        );

        return ctx.ack();
    }

    public Response handleSelectAssignee(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        Action action = req.getPayload().getActions().getFirst();

        String blockId = action.getBlockId();
        String selectedUserId = action.getSelectedUser();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // request 생성
        AssignIssueRequest assignIssueRequest = AssignIssueRequest.create(requestUserId, Long.parseLong(blockId), selectedUserId);

        // 이슈 할당
        Issue issue = issueService.assign(assignIssueRequest);

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
                .name("blue_loading")
        );

        return ctx.ack();
    }

    public Response handleMoreOptions(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        SelectedOption selectedOption = req.getPayload().getActions().getFirst().getSelectedOption();
        MethodsClient client = ctx.client();
        String blockId = req.getPayload().getActions().getFirst().getBlockId();

        // 이슈 조회
        Issue findIssue = issueService.getIssue(Long.parseLong(blockId));

        // 삭제 옵션 선택 시 삭제 확인 모달 오픈
        if (selectedOption.getValue().equals(VALUE_DELETE_ISSUE)) {
            client.viewsOpen(builder -> builder
                    .triggerId(req.getPayload().getTriggerId())
                    .view(IssueModalBuilder.buildDeleteIssueConfirmModal(ISSUE_DELETE_CONFIRM_MODAL, findIssue))
            );
        }

        return ctx.ack();
    }
}
