package com.teuida.jikimi.domain.issue.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IssueQueryRepository {
    private final JPAQueryFactory query;

    
}
