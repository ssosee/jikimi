package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IssueApplicationEntityRepository extends JpaRepository<IssueApplicationEntity, Long> {
    Set<IssueApplicationEntity> findByIssueEntity(IssueEntity issueEntity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update IssueApplicationEntity ia set ia.deleteDateTime = :deleteDateTime where ia.issueEntity.id = :issueId")
    void bulkDelete(Long issueId, LocalDateTime deleteDateTime);
}
