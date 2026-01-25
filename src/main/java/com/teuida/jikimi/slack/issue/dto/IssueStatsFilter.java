package com.teuida.jikimi.slack.issue.dto;

import com.teuida.jikimi.common.enums.IssueStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
public class IssueStatsFilter {
    private final IssueStatus status;
    @Builder.Default
    private final Period period = Period.WEEK;

    public static IssueStatsFilter defaultIssueStateFilter() {
        return IssueStatsFilter.builder().build();
    }

    public LocalDateTime getStartDateTime(LocalDateTime now) {
        if (period == null || period.getDays() == null) {
            return null;
        }
        return now.minusDays(period.getDays());
    }

    @Getter
    @RequiredArgsConstructor
    public enum Period {
        WEEK(7, "최근 7일"),
        MONTH(30, "최근 30일"),
        QUARTER(90, "최근 90일");

        private final Integer days;
        private final String displayName;
    }
}
