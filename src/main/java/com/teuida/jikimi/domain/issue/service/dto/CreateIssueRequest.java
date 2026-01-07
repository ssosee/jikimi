package com.teuida.jikimi.domain.issue.service.dto;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.model.view.ViewState.Value;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import java.util.Map;
import java.util.Set;
import lombok.Builder;

public record CreateIssueRequest(String slackChannelId,
                                 String slackReporterId,
                                 Set<ApplicationType> applicationTypes,
                                 Set<CourseType> courseTypes,
                                 Environment environment,
                                 String title,
                                 String description,
                                 String userEmail) {
    @Builder
    public CreateIssueRequest {
    }

    public static CreateIssueRequest of(ViewSubmissionRequest req, ViewSubmissionContext ctx) {
        Map<String, Map<String, Value>> values = req.getPayload().getView().getState().getValues();
        return CreateIssueRequest.builder().build();
    }
}
