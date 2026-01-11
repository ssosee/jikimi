package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SOLVE;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.service.IssueActionHandlerService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueActionHandler implements SlackHandlerRegistrar {

    private final IssueActionHandlerService issueActionHandlerService;

    @Override
    public void register(App app) {
        app.blockAction(ACTION_ASSIGN_TO_ME, this::handleAssignToMe);
        app.blockAction(ACTION_SELECT_ASSIGNEE, this::handleSelectAssignee);
        app.blockAction(ACTION_SOLVE, this::handleSolve);
    }

    public Response handleAssignToMe(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        return issueActionHandlerService.handleAssignToMe(req, ctx);
    }

    public Response handleSelectAssignee(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        return issueActionHandlerService.handleSelectAssignee(req, ctx);
    }

    public Response handleSolve(BlockActionRequest req, ActionContext ctx) throws SlackApiException, IOException {
        return issueActionHandlerService.handleSolveIssue(req, ctx);
    }
}
