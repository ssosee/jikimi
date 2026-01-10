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
import static com.teuida.jikimi.slack.util.SlackValueParser.extractString;
import static com.teuida.jikimi.slack.util.SlackValueParser.extractStringSet;

import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.model.view.ViewState.Value;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CreateIssueRequest extends IssueRequest {

    private final String slackChannelId;
    private final String slackReporterId;
    private final Set<ApplicationType> applicationTypes;
    private final Set<CourseType> courseTypes;
    private final Set<String> usergroupIds;
    private final Environment environment;
    private final String title;
    private final String description;
    private final String userEmail;

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

    public static CreateIssueRequest of(ViewSubmissionRequest req, ViewSubmissionContext ctx) {
        Map<String, Map<String, Value>> values = req.getPayload().getView().getState().getValues();

        return CreateIssueRequest.builder()
                .requestUserId(ctx.getRequestUserId())
                .slackChannelId(req.getPayload().getView().getPrivateMetadata())
                .slackReporterId(req.getPayload().getUser().getId())
                .environment(extractEnum(values, BLOCK_ENVIRONMENT, ACTION_ENVIRONMENT, Environment.class))
                .courseTypes(extractOptionalEnumSet(values, BLOCK_COURSE_TYPE, ACTION_COURSE_TYPE, CourseType.class))
                .usergroupIds(extractStringSet(values, BLOCK_USERGROUP, ACTION_USERGROUP))
                .applicationTypes(extractEnumSet(values, BLOCK_APPLICATION_TYPE, ACTION_APPLICATION_TYPE, ApplicationType.class))
                .title(extractString(values, BLOCK_TITLE, ACTION_TITLE))
                .description(extractString(values, BLOCK_DESCRIPTION, ACTION_DESCRIPTION))
                .userEmail(extractOptionalString(values, BLOCK_USER_EMAIL, ACTION_USER_EMAIL).orElse(null))
                .build();
    }
}
