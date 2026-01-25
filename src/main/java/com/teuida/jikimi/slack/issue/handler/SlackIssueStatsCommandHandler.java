package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.COMMAND_ISSUE_STATS;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.service.IssueStatsCommandHandlerService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueStatsCommandHandler implements SlackHandlerRegistrar {

    private final IssueStatsCommandHandlerService issueStatsCommandHandlerService;

    @Override
    public void register(App app) {
        app.command(COMMAND_ISSUE_STATS, this::issueStatsCommandHandler);
    }

    public Response issueStatsCommandHandler(SlashCommandRequest req, SlashCommandContext ctx)
            throws SlackApiException, IOException {
        return issueStatsCommandHandlerService.handleCommandIssueStats(req, ctx);
    }
}
