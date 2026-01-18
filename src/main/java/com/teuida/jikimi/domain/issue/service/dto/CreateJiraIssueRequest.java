package com.teuida.jikimi.domain.issue.service.dto;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_JIRA_PRIORITY;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_JIRA_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_JIRA_PRIORITY;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_JIRA_TYPE;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractStringSelectedOption;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.model.view.ViewState.Value;
import com.teuida.jikimi.slack.util.RequestValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CreateJiraIssueRequest extends IssueRequest {
    @NotNull(message = "이슈 ID는 필수 입니다")
    private final Long issueId;

    @NotBlank(message = "우선 순위는 필수 입니다")
    private final String priorityId;

    @NotBlank(message = "이슈 타입은 필수 입니다")
    private final String issueTypeId;

    public CreateJiraIssueRequest(String requestUserId, Long issueId, String priorityId, String issueTypeId) {
        super(requestUserId);
        this.issueId = issueId;
        this.priorityId = priorityId;
        this.issueTypeId = issueTypeId;
    }

    public static CreateJiraIssueRequest create(ViewSubmissionRequest req, ViewSubmissionContext ctx,
                                                RequestValidator validator) {
        Map<String, Map<String, Value>> values = req.getPayload().getView().getState().getValues();

        CreateJiraIssueRequest request = CreateJiraIssueRequest.builder()
                .requestUserId(ctx.getRequestUserId())
                .issueId(Long.parseLong(req.getPayload().getView().getPrivateMetadata()))
                .priorityId(extractStringSelectedOption(values, BLOCK_JIRA_PRIORITY, ACTION_JIRA_PRIORITY))
                .issueTypeId(extractStringSelectedOption(values, BLOCK_JIRA_TYPE, ACTION_JIRA_TYPE))
                .build();

        validator.validate(request);

        return request;
    }
}
