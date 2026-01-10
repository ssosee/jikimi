package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueUsergroupEntityRepository extends JpaRepository<IssueUsergroupEntity, Long> {
    Set<IssueUsergroupEntity> findByIssueEntity(IssueEntity issueEntity);
}
