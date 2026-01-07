package com.teuida.jikimi.slack.controller;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;

// @Slf4j
// @WebServlet("/slack/events")
public class SlackEventController extends SlackAppServlet {

    public SlackEventController(App app) {
        super(app);
        // log.info("SlackEventController initialized");
    }
}
