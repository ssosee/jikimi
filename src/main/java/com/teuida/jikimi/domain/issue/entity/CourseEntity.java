package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.CourseType;
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
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CourseType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private IssueEntity issue;

    @Builder
    private CourseEntity(CourseType type, IssueEntity issue) {
        this.type = type;
        this.issue = issue;
    }

    public static CourseEntity create(CourseType type, IssueEntity issueEntity) {
        return CourseEntity.builder()
                .type(type)
                .issue(issueEntity)
                .build();
    }
}
