package com.teuida.jikimi.domain.issue.service.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DeleteIssueRequest extends IssueRequest {
    private final Long issueId;

    public DeleteIssueRequest(String requestUserId, Long issueId) {
        super(requestUserId);
        this.issueId = issueId;
    }

    public static DeleteIssueRequest create(String requestUserId, Long issueId) {
        return DeleteIssueRequest.builder()
                .requestUserId(requestUserId)
                .issueId(issueId)
                .build();
    }
}
