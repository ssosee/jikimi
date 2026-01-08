package com.teuida.jikimi.slack.issue.handler;

import com.slack.api.bolt.App;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;

public class SlackIssueActionHandler implements SlackHandlerRegistrar {
    @Override
    public void register(App app) {

    }

    @Override
    public String getCallbackId() {
        return "";
    }
}
