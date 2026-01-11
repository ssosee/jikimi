package com.teuida.jikimi.domain.issue.service.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SolveIssueRequest extends IssueRequest {
    private final Long issueId;

    public SolveIssueRequest(String requestUserId, Long issueId) {
        super(requestUserId);
        this.issueId = issueId;
    }

    public static SolveIssueRequest create(String requestUserId, Long issueId) {
        return SolveIssueRequest.builder()
                .requestUserId(requestUserId)
                .issueId(issueId)
                .build();
    }
}
