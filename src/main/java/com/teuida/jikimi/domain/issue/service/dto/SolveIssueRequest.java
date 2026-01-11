package com.teuida.jikimi.domain.issue.service.dto;

import com.teuida.jikimi.slack.util.RequestValidator;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SolveIssueRequest extends IssueRequest {
    @NotNull(message = "이슈 ID는 필수입니다")
    private final Long issueId;

    public SolveIssueRequest(String requestUserId, Long issueId) {
        super(requestUserId);
        this.issueId = issueId;
    }

    public static SolveIssueRequest create(String requestUserId, Long issueId, RequestValidator validator) {
        SolveIssueRequest request = SolveIssueRequest.builder()
                .requestUserId(requestUserId)
                .issueId(issueId)
                .build();

        validator.validate(request);
        return request;
    }
}
