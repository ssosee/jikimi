package com.teuida.jikimi.slack.config;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Bag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SlackConfig {

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
        return new App(appConfig);
    }

    @Bean
    public Slack slack() {
        return Slack.getInstance();
    }
}
