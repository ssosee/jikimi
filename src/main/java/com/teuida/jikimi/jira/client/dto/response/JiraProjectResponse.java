package com.teuida.jikimi.jira.client.dto.response;

import java.util.List;

public record JiraProjectResponse(
        String id,
        String key,
        String name,
        List<IssueType> issueTypes
) {
    public record IssueType(
            String id,
            String name,
            String description,
            boolean subtask
    ) {
    }
}
