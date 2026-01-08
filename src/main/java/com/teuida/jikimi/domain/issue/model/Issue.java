package com.teuida.jikimi.domain.issue.model;

import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record Issue(Long id,
                    String slackMessageTs,
                    String slackChannelId,
                    String slackReporterId,
                    String slackAssigneeId,
                    Set<String> slackAssignedUsergroupIds,
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

    public static Issue create(IssueEntity issueEntity,
                               Set<IssueApplicationEntity> applicationEntities,
                               Set<IssueCourseEntity> courseEntities,
                               Set<IssueUsergroupEntity> issueUsergroupEntities) {
        return Issue.builder()
                .id(issueEntity.getId())
                .slackChannelId(issueEntity.getSlackChannelId())
                .slackReporterId(issueEntity.getSlackReporterId())
                .slackAssignedUsergroupIds(issueUsergroupEntities.stream()
                        .map(IssueUsergroupEntity::getSlackUsergroupId)
                        .collect(Collectors.toSet()))
                .environment(issueEntity.getEnvironment())
                .applicationTypes(applicationEntities.stream()
                        .map(IssueApplicationEntity::getType)
                        .collect(Collectors.toSet()))
                .courseTypes(courseEntities.stream()
                        .map(IssueCourseEntity::getType)
                        .collect(Collectors.toSet()))
                .status(issueEntity.getStatus())
                .title(issueEntity.getTitle())
                .description(issueEntity.getDescription())
                .userEmail(issueEntity.getUserEmail())
                .build();
    }
}
