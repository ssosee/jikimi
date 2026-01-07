package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEntityRepository extends JpaRepository<CourseEntity, Long> {
}
