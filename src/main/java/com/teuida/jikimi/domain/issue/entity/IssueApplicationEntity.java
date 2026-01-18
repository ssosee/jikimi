package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.ApplicationType;
import jakarta.persistence.CascadeType;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "issue_application")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueApplicationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "issue_id", foreignKey = @ForeignKey(name = "fk_issue_application_01"))
    private IssueEntity issueEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ApplicationType type;

    @Column(name = "create_date_time", nullable = false)
    private LocalDateTime createDateTime;

    @Builder
    private IssueApplicationEntity(ApplicationType type, IssueEntity issueEntity, LocalDateTime createDateTime) {
        this.type = type;
        this.issueEntity = issueEntity;
        this.createDateTime = createDateTime;
    }

    public static IssueApplicationEntity create(IssueEntity issueEntity, ApplicationType type, LocalDateTime createDateTime) {
        return IssueApplicationEntity.builder()
                .issueEntity(issueEntity)
                .type(type)
                .createDateTime(createDateTime)
                .build();
    }
}
