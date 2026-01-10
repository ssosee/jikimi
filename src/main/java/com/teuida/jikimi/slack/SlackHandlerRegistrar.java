package com.teuida.jikimi.slack;

import com.slack.api.bolt.App;

public interface SlackHandlerRegistrar {
    void register(App app);
}
