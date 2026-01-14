package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "issue", uniqueConstraints = {
        @UniqueConstraint(name = "uk_issues_01", columnNames = {"slack_assignee_id", "slack_message_ts"}),
        @UniqueConstraint(name = "uk_issues_02", columnNames = {"jira_issue_key"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment")
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private IssueStatus status;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "user_email", length = 50)
    private String userEmail;

    @Column(name = "slack_reporter_id")
    private String slackReporterId;

    @Column(name = "slack_assignee_id")
    private String slackAssigneeId;

    @Column(name = "slack_channel_id")
    private String slackChannelId;

    @Column(name = "slack_message_ts")
    private String slackMessageTs;

    @Column(name = "jira_issue_key")
    private String jiraIssueKey;

    @Column(name = "jira_issue_url")
    private String jiraIssueUrl;

    @Column(name = "jira_assignee_id")
    private String jiraAssigneeId;

    @Builder
    private IssueEntity(Environment environment, IssueStatus status, String title, String description, String userEmail,
                        String slackReporterId, String slackAssigneeId, String slackChannelId, String slackMessageTs,
                        String jiraIssueKey, String jiraIssueUrl, String jiraAssigneeId) {
        this.environment = environment;
        this.status = status;
        this.title = title;
        this.description = description;
        this.userEmail = userEmail;
        this.slackReporterId = slackReporterId;
        this.slackAssigneeId = slackAssigneeId;
        this.slackChannelId = slackChannelId;
        this.slackMessageTs = slackMessageTs;
        this.jiraIssueKey = jiraIssueKey;
        this.jiraIssueUrl = jiraIssueUrl;
        this.jiraAssigneeId = jiraAssigneeId;
    }

    public static IssueEntity create(CreateIssueRequest request) {
        return IssueEntity.builder()
                .slackChannelId(request.getSlackChannelId())
                .slackReporterId(request.getSlackReporterId())
                .environment(request.getEnvironment())
                .status(IssueStatus.OPEN)
                .title(request.getTitle())
                .description(request.getDescription())
                .userEmail(request.getUserEmail())
                .build();
    }

    public void changeSlackMessageTs(String slackMessageTs) {
        if (this.slackMessageTs != null) {
            throw new IllegalStateException("해당 메시지의 타임 스탬프가 이미 존재 합니다.");
        }
        this.slackMessageTs = slackMessageTs;
    }

    public boolean isClosed() {
        return this.status == IssueStatus.CLOSED;
    }

    public boolean isEqualsSlackReporterId(String slackReporterId) {
        return this.slackReporterId.equals(slackReporterId);
    }

    public boolean isEqualsSlackAssigneeId(String slackAssigneeId) {
        return this.slackAssigneeId.equals(slackAssigneeId);
    }

    public void inProgress(String slackAssigneeId) {
        this.status = IssueStatus.IN_PROGRESS;
        this.slackAssigneeId = slackAssigneeId;
    }

    public void solve() {
        if (this.slackAssigneeId == null) {
            throw new IllegalStateException("담당자가 없는 이슈는 해결될 수 없습니다.");
        }
        this.status = IssueStatus.CLOSED;
    }

    public String toJiraLink() {
        return String.format("<%s|%s>", this.jiraIssueUrl, this.jiraIssueKey);
    }
}
