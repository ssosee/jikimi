package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ISSUE_MODAL;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.IssueBlockBuilder;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueModalHandler implements SlackHandlerRegistrar {

    private final IssueService issueService;

    @Override
    public void register(App app) {
        app.viewSubmission(ISSUE_MODAL, this::handle);
    }

    public Response handle(ViewSubmissionRequest req, ViewSubmissionContext ctx) throws SlackApiException, IOException {
        // request 생성
        CreateIssueRequest createIssueRequest = CreateIssueRequest.of(req, ctx);

        // 이슈 생성
        Issue issue = issueService.createIssue(createIssueRequest);

        // 채널에 메시지 전송
        ChatPostMessageResponse response = ctx.client().chatPostMessage(builder -> builder
                .channel(issue.slackChannelId())
                .blocks(IssueBlockBuilder.buildIssueBlocks(issue))
                .text(String.format("%s 이슈가 등록되었습니다.", issue.title()))
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
