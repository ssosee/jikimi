package com.teuida.jikimi.domain.issue.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.entity.QIssueEntity;
import com.teuida.jikimi.domain.issue.entity.QIssueUsergroupEntity;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.dto.IssueStatusCountRow;
import com.teuida.jikimi.slack.issue.dto.NotSolvedIssueRow;
import com.teuida.jikimi.slack.issue.dto.OpenIssueWithUsergroupsRow;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IssueQueryRepository {
    private final JPAQueryFactory query;

    public IssueStatsRow getIssueStats(IssueStatsFilter filter, LocalDateTime now) {
        QIssueEntity issue = QIssueEntity.issueEntity;

        BooleanBuilder condition = buildCondition(filter, issue, now);

        // 상태별 카운트 조회
        List<IssueStatusCountRow> results = query
                .select(Projections.constructor(IssueStatusCountRow.class, issue.status, issue.count().coalesce(0L)))
                .from(issue)
                .where(condition)
                .groupBy(issue.status)
                .fetch();

        Map<IssueStatus, Long> countByStatus = results.stream()
                .collect(Collectors.toMap(IssueStatusCountRow::issueStatus, IssueStatusCountRow::count));

        long totalCount = countByStatus.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        // 미해결 이슈 목록 조회
        List<NotSolvedIssueRow> notSolvedIssueRows = findNotSolvedIssues(filter, now);

        return IssueStatsRow.of(totalCount, countByStatus, notSolvedIssueRows, filter);
    }

    public List<NotSolvedIssueRow> findNotSolvedIssues(IssueStatsFilter filter, LocalDateTime now) {
        QIssueEntity issue = QIssueEntity.issueEntity;

        BooleanBuilder condition = new BooleanBuilder();

        // 기간 조건
        LocalDateTime startDateTime = filter.getStartDateTime(now);
        if (startDateTime != null) {
            condition.and(issue.createDateTime.goe(startDateTime));
        }

        // CLOSED 제외한 상태만 조회
        condition.and(issue.status.ne(IssueStatus.CLOSED));

        return query
                .select(Projections.constructor(NotSolvedIssueRow.class,
                        issue.id,
                        issue.title,
                        issue.slackChannelId,
                        issue.slackMessageTs,
                        issue.slackAssigneeId,
                        issue.createDateTime))
                .from(issue)
                .where(condition)
                .orderBy(issue.createDateTime.desc())
                .fetch();
    }

    public List<OpenIssueWithUsergroupsRow> findOpenIssuesWithUsergroups(IssueStatsFilter filter, LocalDateTime now) {
        List<NotSolvedIssueRow> openIssues = findNotSolvedIssues(filter, now);

        if (openIssues.isEmpty()) {
            return List.of();
        }

        // 이슈 ID 목록 추출
        List<Long> issueIds = openIssues.stream()
                .map(NotSolvedIssueRow::issueId)
                .toList();

        // 이슈별 usergroup 조회
        Map<Long, Set<String>> usergroupsByIssueId = findUsergroupsByIssueIds(issueIds);

        // OpenIssueWithUsergroupsRow로 변환
        return openIssues.stream()
                .map(row -> OpenIssueWithUsergroupsRow.from(
                        row,
                        usergroupsByIssueId.getOrDefault(row.issueId(), Set.of())
                ))
                .toList();
    }

    private Map<Long, Set<String>> findUsergroupsByIssueIds(List<Long> issueIds) {
        QIssueUsergroupEntity usergroup = QIssueUsergroupEntity.issueUsergroupEntity;

        List<com.querydsl.core.Tuple> results = query
                .select(usergroup.issueEntity.id, usergroup.slackUsergroupId)
                .from(usergroup)
                .where(usergroup.issueEntity.id.in(issueIds))
                .fetch();

        return results.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(usergroup.issueEntity.id),
                        Collectors.mapping(
                                tuple -> tuple.get(usergroup.slackUsergroupId),
                                Collectors.toCollection(HashSet::new)
                        )
                ));
    }

    private BooleanBuilder buildCondition(IssueStatsFilter filter, QIssueEntity issue, LocalDateTime now) {
        BooleanBuilder condition = new BooleanBuilder();

        // 기간 조건
        LocalDateTime startDateTime = filter.getStartDateTime(now);
        if (startDateTime != null) {
            condition.and(issue.createDateTime.goe(startDateTime));
        }

        // 상태 조건
        if (filter.getStatus() != null) {
            condition.and(issue.status.eq(filter.getStatus()));
        }

        return condition;
    }
}
