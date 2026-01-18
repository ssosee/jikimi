package com.teuida.jikimi.jira.client;

import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JiraApiClientTest {
    @Value("${jira.project-key}")
    private String projectKey;

    @Autowired
    private JiraApiClient jiraApiClient;

    //@Disabled
    @Test
    @DisplayName("")
    void test() {
        // given

        // when
        JiraProjectResponse project = jiraApiClient.getProject(projectKey);
        JiraSearchPriorityResponse jiraSearchPriority = jiraApiClient.searchPriorities(project.id());

        // then
        System.out.println(project);
        System.out.println(jiraSearchPriority);
    }
}