package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IssueCourseEntityRepository extends JpaRepository<IssueCourseEntity, Long> {
    Set<IssueCourseEntity> findByIssueEntity(IssueEntity issueEntity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update IssueCourseEntity ic set ic.deleteDateTime = :deleteDateTime where ic.issueEntity.id = :issueId")
    void bulkDelete(Long issueId, LocalDateTime deleteDateTime);
}
