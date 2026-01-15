package com.teuida.jikimi.domain.issue.model;

import com.teuida.jikimi.domain.issue.entity.UserMappingEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMapping {
    private final Long id;
    private final String slackUserId;
    private final String slackEmail;
    private final String slackDisplayName;
    private final String jiraAccountId;
    private final String jiraDisplayName;

    public static UserMapping from(UserMappingEntity entity) {
        return UserMapping.builder()
                .id(entity.getId())
                .slackUserId(entity.getSlackUserId())
                .slackEmail(entity.getSlackUserEmail())
                .slackDisplayName(entity.getSlackDisplayName())
                .jiraAccountId(entity.getJiraAccountId())
                .jiraDisplayName(entity.getJiraDisplayName())
                .build();
    }

    public static UserMapping create(String slackUserId, String slackEmail, String slackDisplayName, String jiraAccountId,
                                     String jiraDisplayName) {
        return UserMapping.builder()
                .slackUserId(slackUserId)
                .slackEmail(slackEmail)
                .slackDisplayName(slackDisplayName)
                .jiraAccountId(jiraAccountId)
                .jiraDisplayName(jiraDisplayName)
                .build();
    }
}
