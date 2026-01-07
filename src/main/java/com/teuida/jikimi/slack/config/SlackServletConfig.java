package com.teuida.jikimi.slack.config;

import com.slack.api.bolt.App;
import com.slack.api.bolt.jakarta_servlet.SlackAppServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackServletConfig {

    @Bean
    public ServletRegistrationBean<SlackAppServlet> slackServlet(App app) {
        SlackAppServlet servlet = new SlackAppServlet(app);
        return new ServletRegistrationBean<>(servlet, "/slack/events");
    }
}

