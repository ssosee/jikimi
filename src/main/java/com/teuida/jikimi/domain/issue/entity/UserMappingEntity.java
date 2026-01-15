package com.teuida.jikimi.domain.issue.entity;

import com.slack.api.model.User;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_mapping",
        indexes = {
                @Index(name = "idx_user_mapping_01", columnList = "slack_user_id", unique = true),
                @Index(name = "idx_user_mapping_02", columnList = "slack_email", unique = true)
        })
@SQLRestriction("delete_date_time is null")
public class UserMappingEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slack_user_id", nullable = false, length = 50)
    private String slackUserId;

    @Column(name = "slack_email", nullable = false, length = 50)
    private String slackUserEmail;

    @Column(name = "slack_display_name", length = 50)
    private String slackDisplayName;

    @Column(name = "slack_real_name", length = 50)
    private String slackRealName;

    @Column(name = "jira_account_id", nullable = false, length = 50)
    private String jiraAccountId;

    @Column(name = "jira_display_name", length = 50)
    private String jiraDisplayName;

    @Column(name = "delete_date_time")
    private LocalDateTime deleteDateTime;

    @Builder
    private UserMappingEntity(String slackUserId, String slackUserEmail, String slackDisplayName, String slackRealName,
                              String jiraAccountId, String jiraDisplayName) {
        this.slackUserId = slackUserId;
        this.slackUserEmail = slackUserEmail;
        this.slackDisplayName = slackDisplayName;
        this.slackRealName = slackRealName;
        this.jiraAccountId = jiraAccountId;
        this.jiraDisplayName = jiraDisplayName;
    }

    public static UserMappingEntity create(JiraUserResponse jiraUser, User slackUser) {
        return UserMappingEntity.builder()
                .slackUserId(slackUser.getId())
                .slackUserEmail(slackUser.getProfile().getEmail())
                .slackDisplayName(slackUser.getProfile().getDisplayName())
                .jiraAccountId(jiraUser.accountId())
                .jiraDisplayName(jiraUser.displayName())
                .build();
    }

    public void delete(LocalDateTime deleteDateTime) {
        this.deleteDateTime = deleteDateTime;
    }
}
