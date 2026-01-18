package com.teuida.jikimi.domain.issue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "issue_usergroup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueUsergroupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", foreignKey = @ForeignKey(name = "fk_issue_team_01"))
    private IssueEntity issueEntity;

    private String slackUsergroupId;

    @Column(name = "create_date_time", nullable = false)
    private LocalDateTime createDateTime;

    @Builder
    private IssueUsergroupEntity(IssueEntity issueEntity, String slackUsergroupId, LocalDateTime createDateTime) {
        this.issueEntity = issueEntity;
        this.slackUsergroupId = slackUsergroupId;
        this.createDateTime = createDateTime;
    }

    public static IssueUsergroupEntity create(IssueEntity issueEntity, String slackUsergroupId, LocalDateTime createDateTime) {
        return IssueUsergroupEntity.builder()
                .issueEntity(issueEntity)
                .slackUsergroupId(slackUsergroupId)
                .createDateTime(createDateTime)
                .build();
    }
}
