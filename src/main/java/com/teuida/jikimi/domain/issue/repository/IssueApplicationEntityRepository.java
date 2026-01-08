package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueApplicationEntityRepository extends JpaRepository<IssueApplicationEntity, Long> {
}
