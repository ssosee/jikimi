package com.teuida.jikimi.domain.issue.entity;

import com.teuida.jikimi.common.enums.CourseType;
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
@Table(name = "issue_course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueCourseEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", foreignKey = @ForeignKey(name = "fk_courses_01"))
    private IssueEntity issueEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private CourseType type;

    @Builder
    private IssueCourseEntity(CourseType type, IssueEntity issueEntity) {
        this.type = type;
        this.issueEntity = issueEntity;
    }

    public static IssueCourseEntity create(IssueEntity issueEntity, CourseType type) {
        return IssueCourseEntity.builder()
                .issueEntity(issueEntity)
                .type(type)
                .build();
    }
}
