package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.ApplicationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "applications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ApplicationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private IssueEntity issueEntity;

    @Builder
    private ApplicationEntity(ApplicationType type, IssueEntity issueEntity) {
        this.type = type;
        this.issueEntity = issueEntity;
    }

    public static ApplicationEntity create(ApplicationType type, IssueEntity issueEntity) {
        return ApplicationEntity.builder()
                .type(type)
                .issueEntity(issueEntity)
                .build();
    }
}
