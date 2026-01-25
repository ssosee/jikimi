package com.teuida.jikimi.slack.issue.service;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.JIRA_ISSUE_MODAL;

import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.model.Issue.JiraContext;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.AssignIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.SolveIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraTransitionsResponse;
import com.teuida.jikimi.jira.service.JiraMetadataService;
import com.teuida.jikimi.jira.service.JiraService;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import com.teuida.jikimi.slack.issue.IssueModalBuilder;
import com.teuida.jikimi.slack.util.RequestValidator;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Slack handler 로직을 담당하는 서비스 레이어 AOP 적용을 위해 Spring Bean으로 분리
 */
@Service
@RequiredArgsConstructor
public class IssueActionHandlerService {

    private final IssueService issueService;
    private final RequestValidator requestValidator;
    private final JiraMetadataService jiraMetadataService;
    private final JiraService jiraService;

    public Response handleAssignToMe(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        String blockId = req.getPayload().getActions().getFirst().getBlockId();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // request 생성 및 검증
        AssignIssueRequest assignIssueRequest = AssignIssueRequest.create(requestUserId,
                Long.parseLong(blockId), requestUserId, requestValidator);

        // 이슈 할당
        Issue issue = issueService.assign(assignIssueRequest);
        String channelId = issue.getSlackContext().getChannelId();
        String messageTs = issue.getSlackContext().getMessageTs();

        // 슬랙 메시지 원본 수정
        client.chatUpdate(builder -> builder
                .channel(channelId)
                .ts(messageTs)
                .blocks(IssueBlockBuilder.buildIssueBlocks(issue))
        );

        // 해당 스레드에 이슈 할당 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(channelId)
                .token(ctx.getBotToken())
                .threadTs(messageTs)
                .text(String.format("🙌 <@%s>님이 본인에게 이슈를 *할당* 했습니다.", requestUserId))
        );

        // 해당 스레드에 이모지 추가
        client.reactionsAdd(builder -> builder
                .token(ctx.getBotToken())
                .channel(channelId)
                .timestamp(messageTs)
                .name("blue_loading")
        );

        return ctx.ack();
    }

    public Response handleSelectAssignee(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        var action = req.getPayload().getActions().getFirst();

        String blockId = action.getBlockId();
        String selectedUserId = action.getSelectedUser();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        // request 생성 및 검증
        AssignIssueRequest assignIssueRequest = AssignIssueRequest.create(requestUserId,
                Long.parseLong(blockId), selectedUserId, requestValidator);

        // 이슈 할당
        Issue assignedIssue = issueService.assign(assignIssueRequest);
        String channelId = assignedIssue.getSlackContext().getChannelId();
        String messageTs = assignedIssue.getSlackContext().getMessageTs();

        // 슬랙 메시지 원본 수정
        client.chatUpdate(builder -> builder
                .channel(channelId)
                .ts(messageTs)
                .blocks(IssueBlockBuilder.buildIssueBlocks(assignedIssue))
        );

        // 해당 스레드에 이슈 할당 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(channelId)
                .token(ctx.getBotToken())
                .threadTs(messageTs)
                .text(String.format("🕊️ <@%s>님이 <@%s>에게 이슈를 *할당* 했습니다.", requestUserId, selectedUserId))
        );

        // 해당 스레드에 이모지 추가
        client.reactionsAdd(builder -> builder
                .token(ctx.getBotToken())
                .channel(channelId)
                .timestamp(messageTs)
                .name("blue_loading")
        );

        return ctx.ack();
    }

    public Response handleSolveIssue(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        String blockId = req.getPayload().getActions().getFirst().getBlockId();
        String requestUserId = ctx.getRequestUserId();
        MethodsClient client = ctx.client();

        SolveIssueRequest solveIssueRequest = SolveIssueRequest.create(requestUserId,
                Long.parseLong(blockId), requestValidator);

        // 이슈 해결
        Issue solvedIssue = issueService.solve(solveIssueRequest);
        String channelId = solvedIssue.getSlackContext().getChannelId();
        String messageTs = solvedIssue.getSlackContext().getMessageTs();

        // 이슈에 Jira 티켓이 존재하는 경우
        if (solvedIssue.getJiraContext().isCreated()) {
            JiraContext jiraContext = solvedIssue.getJiraContext();
            String jiraIssueKey = jiraContext.getIssueKey();

            // Jira 사용 가능한 전환 조회
            JiraTransitionsResponse transitions = jiraMetadataService.getTransitions(jiraIssueKey);

            // Jira 티켓을 완료 상태로 변경
            jiraService.completeIssue(transitions.transitions(), jiraIssueKey);
        }

        // 해당 스레드에 이슈 해결 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(channelId)
                .token(ctx.getBotToken())
                .threadTs(messageTs)
                .text(String.format("🎉 <@%s>님이 이슈를 *해결* 했습니다.", requestUserId))
        );

        // 슬랙 메시지 원본 수정
        client.chatUpdate(builder -> builder
                .channel(channelId)
                .ts(messageTs)
                .blocks(IssueBlockBuilder.buildIssueBlocks(solvedIssue))
        );

        // 해당 스레드에 이모지 제거
        client.reactionsRemove(builder -> builder
                .token(ctx.getBotToken())
                .channel(channelId)
                .timestamp(messageTs)
                .name("blue_loading")
        );

        // 해당 스레드에 이모지 추가
        client.reactionsAdd(builder -> builder
                .token(ctx.getBotToken())
                .channel(channelId)
                .timestamp(messageTs)
                .name("done")
        );

        return ctx.ack();
    }

    public Response handleCreateJiraIssue(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        String blockId = req.getPayload().getActions().getFirst().getBlockId();
        Long issueId = Long.parseLong(blockId);

        // 이슈만 조회
        Issue findIssue = issueService.getOnlyIssue(issueId);

        // Jira 프로젝트 조회
        JiraProjectResponse jiraProject = jiraMetadataService.getProject();

        // Jira 우선순위 조회
        JiraSearchPriorityResponse jiraSearchPriority = jiraMetadataService.searchPriorities(jiraProject.id());

        // 모달 생성
        ctx.client().viewsOpen(builder -> builder
                .triggerId(ctx.getTriggerId())
                .view(IssueModalBuilder.buildJiraIssueModal(findIssue, JIRA_ISSUE_MODAL, jiraProject, jiraSearchPriority))
        );

        return ctx.ack();
    }
}
