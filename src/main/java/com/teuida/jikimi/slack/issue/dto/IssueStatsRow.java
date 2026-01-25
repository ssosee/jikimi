package com.teuida.jikimi.slack.issue.dto;

import com.teuida.jikimi.common.enums.IssueStatus;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IssueStatsRow {
    private final long totalCount;
    private final Map<IssueStatus, Long> countByStatus;
    private final List<NotSolvedIssueRow> notSolvedIssueRows;
    private final IssueStatsFilter filter;

    public static IssueStatsRow of(long totalCount, Map<IssueStatus, Long> countByStatus,
                                   List<NotSolvedIssueRow> notSolvedIssueRows,
                                   IssueStatsFilter filter) {
        return IssueStatsRow.builder()
                .totalCount(totalCount)
                .countByStatus(countByStatus)
                .notSolvedIssueRows(notSolvedIssueRows)
                .filter(filter)
                .build();
    }

    public long getCount(IssueStatus status) {
        return countByStatus.getOrDefault(status, 0L);
    }

    public double getPercentage(IssueStatus status) {
        if (totalCount == 0) {
            return 0.0;
        }
        return (getCount(status) * 100.0) / totalCount;
    }

    public String getPeriodDisplayName() {
        return filter.getPeriod().getDisplayName();
    }

    public boolean isEmpty() {
        return totalCount == 0;
    }
}
