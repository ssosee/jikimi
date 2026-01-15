package com.teuida.jikimi.slack.issue.service;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import com.teuida.jikimi.slack.util.RequestValidator;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueModalHandlerService {

    private final IssueService issueService;
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
}
