package com.teuida.jikimi.jira.service.dto;

import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import lombok.Builder;

@Builder
public record JiraIssue(String key,
                        String apiUrl,
                        String assigneeId) {

    public static JiraIssue of(JiraIssueResponse response, String assigneeId) {
        return JiraIssue.builder()
                .key(response.key())
                .apiUrl(response.self())
                .assigneeId(assigneeId)
                .build();
    }

    // 웹 브라우저용 URL 생성
    public String getBrowserUrl() {
        // self: "https://teuida.atlassian.net/rest/api/3/issue/17013"
        // -> "https://teuida.atlassian.net/browse/XTDL-123"
        String baseUrl = apiUrl.substring(0, apiUrl.indexOf("/rest"));
        return baseUrl + "/browse/" + key;
    }
}