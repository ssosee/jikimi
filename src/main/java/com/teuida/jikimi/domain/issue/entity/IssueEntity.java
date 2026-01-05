package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(name = "issue", uniqueConstraints = {
        @UniqueConstraint(name = "uk_issues_01", columnNames = {"slack_assignee_id", "slack_message_ts"}),
        @UniqueConstraint(name = "uk_issues_02", columnNames = {"jira_issue_key"})
})
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
}
