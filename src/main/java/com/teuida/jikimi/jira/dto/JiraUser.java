package com.teuida.jikimi.jira.dto;

public record JiraUser(String accountId,
                       String accountType,
                       boolean active,
                       String displayName,
                       String key,
                       String name,
                       String self) {
}
