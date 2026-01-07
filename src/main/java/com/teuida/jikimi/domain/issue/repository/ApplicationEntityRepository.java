package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationEntityRepository extends JpaRepository<ApplicationEntity, Long> {
}
