package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueLogEntityRepository extends JpaRepository<IssueLogEntity, Long> {
}
