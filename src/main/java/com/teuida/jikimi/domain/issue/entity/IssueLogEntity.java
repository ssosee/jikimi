package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.ActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "issue_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueLogEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", foreignKey = @ForeignKey(name = "fk_issue_log_01"))
    private IssueEntity issue;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    private String slackActorId;

    @Builder
    private IssueLogEntity(IssueEntity issue, ActionType actionType, String slackActorId) {
        this.issue = issue;
        this.actionType = actionType;
        this.slackActorId = slackActorId;
    }

    public static IssueLogEntity create(IssueEntity issueEntity, ActionType actionType, String slackActorId) {
        return IssueLogEntity.builder()
                .issue(issueEntity)
                .actionType(actionType)
                .slackActorId(slackActorId)
                .build();
    }
}
