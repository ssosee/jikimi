package com.teuida.jikimi.domain.issue.service.dto;

import com.teuida.jikimi.slack.util.RequestValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class AssignIssueRequest extends IssueRequest {
    @NotNull(message = "이슈 ID는 필수입니다")
    private Long issueId;

    @NotBlank(message = "담당자 ID는 필수입니다")
    private String slackAssigneeId;

    public AssignIssueRequest(String requestUserId, Long issueId, String slackAssigneeId) {
        super(requestUserId);
        this.issueId = issueId;
        this.slackAssigneeId = slackAssigneeId;
    }

    public static AssignIssueRequest create(String requestUserId, Long issueId, String slackAssigneeId, RequestValidator validator) {
        AssignIssueRequest request = AssignIssueRequest.builder()
                .requestUserId(requestUserId)
                .issueId(issueId)
                .slackAssigneeId(slackAssigneeId)
                .build();

        validator.validate(request);
        return request;
    }
}
