package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_PUBLISH_STATS;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_TAG_ASSIGNEES;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.service.IssueStatsActionHandlerService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueStatsActionHandler implements SlackHandlerRegistrar {

    private final IssueStatsActionHandlerService issueStatsActionHandlerService;

    @Override
    public void register(App app) {
        app.blockAction(ACTION_TAG_ASSIGNEES, this::handleTagAssignees);
        app.blockAction(ACTION_PUBLISH_STATS, this::handlePublishStats);
    }

    public Response handleTagAssignees(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        return issueStatsActionHandlerService.handleTagAssignees(req, ctx);
    }

    public Response handlePublishStats(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        return issueStatsActionHandlerService.handlePublishStats(req, ctx);
    }
}
