package com.teuida.jikimi.slack.issue.parser;

import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class IssueStatsParameterParser {

    private static final Pattern STATUS_PATTERN = Pattern.compile("status:(open|in_progress|closed)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERIOD_PATTERN = Pattern.compile("period:(today|week|month|quater)", Pattern.CASE_INSENSITIVE);

    public static IssueStatsFilter parse(String commandText) {
        if (commandText == null || commandText.isBlank()) {
            return IssueStatsFilter.defaultIssueStateFilter();
        }

        IssueStatsFilter.IssueStatsFilterBuilder builder = IssueStatsFilter.builder();

        // status 파싱
        Matcher statusMatcher = STATUS_PATTERN.matcher(commandText);
        if (statusMatcher.find()) {
            String statusValue = statusMatcher.group(1).toUpperCase();
            builder.status(IssueStatus.valueOf(statusValue));
        }

        // period 파싱
        Matcher periodMatcher = PERIOD_PATTERN.matcher(commandText);
        if (periodMatcher.find()) {
            String periodValue = periodMatcher.group(1).toUpperCase();
            builder.period(Period.valueOf(periodValue));
        }

        return builder.build();
    }
}
