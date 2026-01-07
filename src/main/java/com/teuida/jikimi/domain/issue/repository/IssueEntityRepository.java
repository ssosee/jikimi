package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueEntityRepository extends JpaRepository<IssueEntity, Long> {
}
