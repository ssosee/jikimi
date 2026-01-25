package com.teuida.jikimi.slack.issue.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record NotSolvedIssueRow(
        Long issueId,
        String title,
        String slackChannelId,
        String slackMessageTs,
        String slackAssigneeId,
        LocalDateTime createDateTime
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yy/MM/dd");

    public String getFormattedDate() {
        return createDateTime.format(DATE_FORMATTER);
    }

    public String getThreadLink(String workspaceUrl) {
        return String.format("%s/archives/%s/p%s",
                workspaceUrl,
                slackChannelId,
                slackMessageTs.replace(".", ""));
    }

    public String getAssigneeDisplay() {
        return slackAssigneeId != null
                ? "<@" + slackAssigneeId + ">"
                : "담당자 없음";
    }

    public boolean hasAssignee() {
        return slackAssigneeId != null;
    }
}
