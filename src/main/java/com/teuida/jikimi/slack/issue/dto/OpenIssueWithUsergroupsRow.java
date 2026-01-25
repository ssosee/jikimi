package com.teuida.jikimi.slack.issue.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record OpenIssueWithUsergroupsRow(
        Long issueId,
        String title,
        String slackChannelId,
        String slackMessageTs,
        String slackAssigneeId,
        Set<String> slackUsergroupIds,
        LocalDateTime createDateTime
) {
    public static OpenIssueWithUsergroupsRow from(NotSolvedIssueRow row, Set<String> usergroupIds) {
        return new OpenIssueWithUsergroupsRow(
                row.issueId(),
                row.title(),
                row.slackChannelId(),
                row.slackMessageTs(),
                row.slackAssigneeId(),
                usergroupIds,
                row.createDateTime()
        );
    }

    public boolean hasAssignee() {
        return slackAssigneeId != null;
    }

    public boolean hasUsergroups() {
        return slackUsergroupIds != null && !slackUsergroupIds.isEmpty();
    }

    public String getFirstUsergroupId() {
        if (!hasUsergroups()) {
            return null;
        }
        return slackUsergroupIds.iterator().next();
    }
}
