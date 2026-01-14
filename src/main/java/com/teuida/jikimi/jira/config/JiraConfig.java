package com.teuida.jikimi.jira.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class JiraConfig {

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String jiraEmail;

    @Value("${jira.token}")
    private String jiraApiToken;

    @Bean
    public RestTemplate jiraRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.setBasicAuth(jiraEmail, jiraApiToken);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
