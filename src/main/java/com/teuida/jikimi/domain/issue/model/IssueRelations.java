package com.teuida.jikimi.domain.issue.model;

import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IssueEntity와 연관된 관계 엔티티들을 묶어서 관리하는 record.
 * 관계 엔티티 조회 로직의 중복을 줄이고, Issue 도메인 객체 생성을 단순화합니다.
 */
public record IssueRelations(
        Set<IssueApplicationEntity> applications,
        Set<IssueCourseEntity> courses,
        Set<IssueUsergroupEntity> usergroups
) {
    /**
     * ApplicationEntity 컬렉션에서 ApplicationType Set으로 변환
     */
    public Set<ApplicationType> applicationTypes() {
        return applications.stream()
                .map(IssueApplicationEntity::getType)
                .collect(Collectors.toSet());
    }

    /**
     * CourseEntity 컬렉션에서 CourseType Set으로 변환
     */
    public Set<CourseType> courseTypes() {
        return courses.stream()
                .map(IssueCourseEntity::getType)
                .collect(Collectors.toSet());
    }

    /**
     * UsergroupEntity 컬렉션에서 Slack usergroup ID Set으로 변환
     */
    public Set<String> usergroupIds() {
        return usergroups.stream()
                .map(IssueUsergroupEntity::getSlackUsergroupId)
                .collect(Collectors.toSet());
    }
}
