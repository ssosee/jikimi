package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueCourseEntityRepository extends JpaRepository<IssueCourseEntity, Long> {
    Set<IssueCourseEntity> findByIssueEntity(IssueEntity issueEntity);
}
