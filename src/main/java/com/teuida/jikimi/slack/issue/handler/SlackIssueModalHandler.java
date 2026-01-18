package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ISSUE_MODAL;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.JIRA_ISSUE_MODAL;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.service.IssueModalHandlerService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueModalHandler implements SlackHandlerRegistrar {

    private final IssueModalHandlerService issueModalHandlerService;

    @Override
    public void register(App app) {
        app.viewSubmission(ISSUE_MODAL, this::handleCreateIssueModal);
        app.viewSubmission(JIRA_ISSUE_MODAL, this::handleCreateJiraIssueModal);
    }

    public Response handleCreateIssueModal(ViewSubmissionRequest req, ViewSubmissionContext ctx)
            throws SlackApiException, IOException {
        return issueModalHandlerService.handleCreateIssue(req, ctx);
    }

    public Response handleCreateJiraIssueModal(ViewSubmissionRequest req, ViewSubmissionContext ctx)
            throws SlackApiException, IOException {
        return issueModalHandlerService.handleCreateJiraIssue(req, ctx);
    }
}
