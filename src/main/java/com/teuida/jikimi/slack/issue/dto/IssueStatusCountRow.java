package com.teuida.jikimi.slack.issue.dto;

import com.teuida.jikimi.common.enums.IssueStatus;

public record IssueStatusCountRow(IssueStatus issueStatus, Long count) {
}
