package com.teuida.jikimi.domain.issue.model;

import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.entity.ApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.CourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

public record Issue(Long id,
                    String slackMessageTs,
                    String slackChannelId,
                    String slackReporterId,
                    String slackAssigneeId,
                    Environment environment,
                    IssueStatus status,
                    Set<ApplicationType> applicationTypes,
                    Set<CourseType> courseTypes,
                    String title,
                    String description,
                    String userEmail,
                    String jiraIssueKey,
                    String jiraIssueUrl,
                    String jiraAssigneeId) {
    @Builder
    public Issue {
    }

    public static Issue create(IssueEntity issueEntity,
                               Set<ApplicationEntity> applicationEntities,
                               Set<CourseEntity> courseEntities) {
        return Issue.builder()
                .id(issueEntity.getId())
                .slackChannelId(issueEntity.getSlackChannelId())
                .slackReporterId(issueEntity.getSlackReporterId())
                .environment(issueEntity.getEnvironment())
                .applicationTypes(applicationEntities.stream()
                        .map(ApplicationEntity::getType)
                        .collect(Collectors.toSet()))
                .courseTypes(courseEntities.stream()
                        .map(CourseEntity::getType)
                        .collect(Collectors.toSet()))
                .status(issueEntity.getStatus())
                .title(issueEntity.getTitle())
                .description(issueEntity.getDescription())
                .userEmail(issueEntity.getUserEmail())
                .build();
    }
}
