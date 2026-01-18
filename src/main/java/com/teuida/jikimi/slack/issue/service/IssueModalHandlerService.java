package com.teuida.jikimi.slack.issue.service;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.model.Issue.JiraContext;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.service.JiraService;
import com.teuida.jikimi.jira.service.dto.JiraIssue;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import com.teuida.jikimi.slack.util.RequestValidator;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueModalHandlerService {

    private final IssueService issueService;
    private final JiraService jiraService;
    private final RequestValidator requestValidator;

    public Response handleCreateIssue(ViewSubmissionRequest req, ViewSubmissionContext ctx)
            throws SlackApiException, IOException {
        // request 생성 및 검증 (ValidationException 발생 가능)
        CreateIssueRequest createIssueRequest = CreateIssueRequest.of(req, ctx, requestValidator);

        // 이슈 생성
        Issue issue = issueService.createIssue(createIssueRequest);

        // 채널에 메시지 전송
        ChatPostMessageResponse response = ctx.client().chatPostMessage(builder -> builder
                .channel(issue.getSlackContext().getChannelId())
                .blocks(IssueBlockBuilder.buildIssueBlocks(issue))
                .text(String.format("%s 이슈가 등록되었습니다.", issue.getTitle()))
                .unfurlLinks(false)
                .unfurlMedia(false)
        );

        if (response.isOk()) {
            String messageTs = response.getMessage().getTs();
            issueService.updateIssue(issue, messageTs);
        }

        return ctx.ack();
    }

    public Response handleCreateJiraIssue(ViewSubmissionRequest req, ViewSubmissionContext ctx)
            throws SlackApiException, IOException {
        MethodsClient client = ctx.client();
        CreateJiraIssueRequest request = CreateJiraIssueRequest.create(req, ctx, requestValidator);

        Long issueId = request.getIssueId();
        String issueTypeId = request.getIssueTypeId();
        String priorityId = request.getPriorityId();

        // 이슈 조회
        Issue findIssue = issueService.getIssue(issueId);

        // Jira 티켓 생성
        JiraIssue jiraIssue = jiraService.createIssue(findIssue, issueTypeId, priorityId);

        // Jira 이슈 정보 등록
        Issue appliedJiraIssue = issueService.applyJiraIssue(request, jiraIssue);
        String channelId = appliedJiraIssue.getSlackContext().getChannelId();
        String messageTs = appliedJiraIssue.getSlackContext().getMessageTs();

        JiraContext jiraContext = appliedJiraIssue.getJiraContext();

        // 슬랙 메시지 원본 수정
        client.chatUpdate(builder -> builder
                .channel(channelId)
                .ts(messageTs)
                .blocks(IssueBlockBuilder.buildIssueBlocks(appliedJiraIssue))
        );

        // 해당 스레드에 Jira 이슈 생성 메시지 추가
        client.chatPostMessage(builder -> builder
                .channel(channelId)
                .token(ctx.getBotToken())
                .threadTs(messageTs)
                .text(String.format(":nyancat_big: <@%s>님이 <%s | %s 티켓>을 *생성* 했습니다.\n",
                        request.getRequestUserId(),
                        jiraContext.getIssueBrowserUrl(),
                        jiraContext.getIssueKey()))
        );

        return ctx.ack();
    }
}
