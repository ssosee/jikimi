package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.UserMappingEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMappingEntityRepository extends JpaRepository<UserMappingEntity, Long> {
    Optional<UserMappingEntity> findBySlackUserId(String slackUserId);
}
