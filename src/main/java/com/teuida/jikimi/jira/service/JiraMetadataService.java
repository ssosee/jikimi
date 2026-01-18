package com.teuida.jikimi.jira.service;

import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraTransitionsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JiraMetadataService {
    
    private final JiraApiClient jiraApiClient;

    @Value(value = "${jira.project-key}")
    private String jiraProjectKey;

    @Cacheable(value = "jira-project", unless = "#result == null")
    public JiraProjectResponse getProject() {
        return jiraApiClient.getProject(jiraProjectKey);
    }

    @Cacheable(key = "#projectId", value = "jira-priority", unless = "#result == null")
    public JiraSearchPriorityResponse searchPriorities(String projectId) {
        return jiraApiClient.searchPriorities(projectId);
    }

    @Cacheable(key = "#issueIdOrKey", value = "jira-transition", unless = "#result == null")
    public JiraTransitionsResponse getTransitions(String issueIdOrKey) {
        return jiraApiClient.getTransitions(issueIdOrKey);
    }
}
