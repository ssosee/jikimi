package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IssueUsergroupEntityRepository extends JpaRepository<IssueUsergroupEntity, Long> {
    Set<IssueUsergroupEntity> findByIssueEntity(IssueEntity issueEntity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update IssueUsergroupEntity iu set iu.deleteDateTime = :deleteDateTime where iu.issueEntity.id = :issueId")
    void bulkDelete(Long issueId, LocalDateTime deleteDateTime);
}
