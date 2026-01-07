package com.teuida.jikimi.slack.config;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SlackConfig {

    private final List<SlackHandlerRegistrar> handlerRegistrars;

    @Value("${slack.signing-secret}")
    private String signingSecret;

    @Value("${slack.bot-token}")
    private String botToken;

    @Bean
    public AppConfig appConfig() {
        return AppConfig.builder()
                .signingSecret(signingSecret)
                .singleTeamBotToken(botToken)
                .build();
    }

    @Bean
    public App slackApp(AppConfig appConfig) {
        App app = new App(appConfig);
        handlerRegistrars.forEach(registrar -> registrar.register(app));
        return app;
    }

    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }
}
