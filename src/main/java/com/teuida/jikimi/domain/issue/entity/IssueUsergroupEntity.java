package com.teuida.jikimi.domain.issue.entity;

import jakarta.persistence.Entity;
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
@Table(name = "issue_usergroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueUsergroupEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", foreignKey = @ForeignKey(name = "fk_issue_team_01"))
    private IssueEntity issueEntity;

    private String slackUsergroupId;

    @Builder
    private IssueUsergroupEntity(IssueEntity issueEntity, String slackUsergroupId) {
        this.issueEntity = issueEntity;
        this.slackUsergroupId = slackUsergroupId;
    }

    public static IssueUsergroupEntity create(IssueEntity issueEntity, String slackUsergroupId) {
        return IssueUsergroupEntity.builder()
                .issueEntity(issueEntity)
                .slackUsergroupId(slackUsergroupId)
                .build();
    }
}
