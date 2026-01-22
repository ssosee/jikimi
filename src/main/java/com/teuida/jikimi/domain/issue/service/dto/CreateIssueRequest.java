package com.teuida.jikimi.domain.issue.service.dto;

import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_APPLICATION_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_COURSE_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_DESCRIPTION;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ENVIRONMENT;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USER_EMAIL;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_APPLICATION_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_COURSE_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_DESCRIPTION;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_ENVIRONMENT;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USER_EMAIL;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractEnum;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractEnumSet;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractOptionalEnumSet;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractOptionalString;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractStringSet;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractStringValue;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.model.view.ViewState.Value;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.validation.ValidCourseTypes;
import com.teuida.jikimi.slack.util.RequestValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CreateIssueRequest extends IssueRequest {

    @NotBlank(message = "슬랙 채널 ID는 필수입니다")
    private final String slackChannelId;

    @NotBlank(message = "슬랙 제보자 ID는 필수입니다")
    private final String slackReporterId;

    @NotEmpty(message = "애플리케이션 타입을 최소 1개 선택해야 합니다")
    private final Set<ApplicationType> applicationTypes;

    @ValidCourseTypes
    private final Set<CourseType> courseTypes;  // Optional

    @NotEmpty(message = "담당 팀을 최소 1개 선택해야 합니다")
    private final Set<String> usergroupIds;

    @NotNull(message = "환경을 선택해야 합니다")
    private final Environment environment;

    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 1, max = 200, message = "제목은 1자 이상 200자 이하여야 합니다")
    private final String title;

    @NotBlank(message = "설명은 필수입니다")
    @Size(min = 1, max = 3000, message = "설명은 1자 이상 3000자 이하여야 합니다")
    private final String description;

    @Email(message = "유효한 이메일 형식이어야 합니다")
    private final String userEmail;  // Optional

    public CreateIssueRequest(String requestUserId, String slackChannelId, String slackReporterId,
                              Set<ApplicationType> applicationTypes, Set<CourseType> courseTypes, Set<String> usergroupIds,
                              Environment environment, String title, String description, String userEmail) {
        super(requestUserId);
        this.slackChannelId = slackChannelId;
        this.slackReporterId = slackReporterId;
        this.applicationTypes = applicationTypes;
        this.courseTypes = courseTypes;
        this.usergroupIds = usergroupIds;
        this.environment = environment;
        this.title = title;
        this.description = description;
        this.userEmail = userEmail;
    }

    public static CreateIssueRequest of(ViewSubmissionRequest req, ViewSubmissionContext ctx, RequestValidator validator) {
        Map<String, Map<String, Value>> values = req.getPayload().getView().getState().getValues();

        CreateIssueRequest request = CreateIssueRequest.builder()
                .requestUserId(ctx.getRequestUserId())
                .slackChannelId(req.getPayload().getView().getPrivateMetadata())
                .slackReporterId(req.getPayload().getUser().getId())
                .environment(extractEnum(values, BLOCK_ENVIRONMENT, ACTION_ENVIRONMENT, Environment.class))
                .courseTypes(extractOptionalEnumSet(values, BLOCK_COURSE_TYPE, ACTION_COURSE_TYPE, CourseType.class))
                .usergroupIds(extractStringSet(values, BLOCK_USERGROUP, ACTION_USERGROUP))
                .applicationTypes(extractEnumSet(values, BLOCK_APPLICATION_TYPE, ACTION_APPLICATION_TYPE, ApplicationType.class))
                .title(extractStringValue(values, BLOCK_TITLE, ACTION_TITLE))
                .description(extractStringValue(values, BLOCK_DESCRIPTION, ACTION_DESCRIPTION))
                .userEmail(extractOptionalString(values, BLOCK_USER_EMAIL, ACTION_USER_EMAIL).orElse(null))
                .build();

        // Validate the request
        validator.validate(request);

        return request;
    }

    public String createPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Title:").append(title).append("\n");
        prompt.append("Description:").append(description).append("\n");
        prompt.append("Environment:").append(environment).append("\n");
        prompt.append("Application Types:").append(String.join(", ",
                applicationTypes.stream().map(Enum::name).toList())).append("\n");
        prompt.append("Course Types:").append(String.join(", ",
                courseTypes.stream().map(Enum::name).toList())).append("\n");

        return prompt.toString();
    }
}
