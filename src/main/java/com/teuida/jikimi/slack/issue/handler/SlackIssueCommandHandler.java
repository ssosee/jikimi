package com.teuida.jikimi.slack.issue.handler;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.COMMAND_ISSUE;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.issue.service.IssueCommandHandlerService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueCommandHandler implements SlackHandlerRegistrar {

    private final IssueCommandHandlerService issueCommandHandlerService;

    @Override
    public void register(App app) {
        app.command(COMMAND_ISSUE, this::issueCommandHandler);
    }

    public Response issueCommandHandler(SlashCommandRequest req, SlashCommandContext ctx) throws SlackApiException, IOException {
        return issueCommandHandlerService.handleCommandIssue(req, ctx);
    }
}
