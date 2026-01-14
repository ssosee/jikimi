package com.teuida.jikimi.jira.client.dto.response;

public record JiraUserResponse(String accountId,
                               String accountType,
                               boolean active,
                               String displayName,
                               String key,
                               String name,
                               String self) {
}
