package com.teuida.jikimi.openai.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class OpenAiFeignConfig {

    @Bean
    public RequestInterceptor openAiRequestInterceptor(@Value("${open-ai.token}") String token) {
        return requestTemplate -> {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            requestTemplate.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
