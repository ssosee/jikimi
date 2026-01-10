package com.teuida.jikimi.domain.issue.service.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AssignIssueRequest extends IssueRequest {
    private Long issueId;
    private String slackAssigneeId;

    public AssignIssueRequest(String requestUserId, Long issueId, String slackAssigneeId) {
        super(requestUserId);
        this.issueId = issueId;
        this.slackAssigneeId = slackAssigneeId;
    }

    public static AssignIssueRequest create(String requestUserId, Long issueId, String slackAssigneeId) {
        return AssignIssueRequest.builder()
                .requestUserId(requestUserId)
                .issueId(issueId)
                .slackAssigneeId(slackAssigneeId)
                .build();
    }
}
