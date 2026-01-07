package com.teuida.jikimi.slack.issue.handler;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackModalHandler implements SlackHandlerRegistrar {

    private final IssueService issueService;

    @Override
    public void register(App app) {
        app.viewSubmission("issue_create_modal", this::issueCommandSubmitHandler);
    }

    @Override
    public String getCallbackId() {
        return "issue_create_modal";
    }

    public Response issueCommandSubmitHandler(ViewSubmissionRequest req, ViewSubmissionContext ctx) {
        Issue issue = issueService.createIssue(CreateIssueRequest.of(req, ctx));
        return ctx.ack();
    }
}
