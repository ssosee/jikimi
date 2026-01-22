package com.teuida.jikimi.jira.config;

import feign.RequestInterceptor;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class JiraFeignConfig {

    @Bean
    public RequestInterceptor jiraRequestInterceptor(@Value("${jira.email}") String email,
                                                     @Value("${jira.token}") String token) {

        String auth = email + ":" + token;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        return requestTemplate -> {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
            requestTemplate.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        };
    }
}
