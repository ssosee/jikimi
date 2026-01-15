package com.teuida.jikimi.jira.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

import com.slack.api.model.User;
import com.slack.api.model.User.Profile;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.UserMappingService;
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
import com.teuida.jikimi.slack.service.SlackApiService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JiraServiceTest {

    @Mock
    UserMappingService userMappingService;
    @Mock
    private JiraApiClient jiraApiClient;
    @Mock
    private SlackApiService slackApiService;
    @InjectMocks
    private JiraService jiraService;

    @Test
    @DisplayName("슬랙 담당자의 이메일로 Jira 사용자를 찾아 이슈를 생성한다")
    void createIssue_success() {
        // given
        ReflectionTestUtils.setField(jiraService, "projectKey", "PROJ");
        ReflectionTestUtils.setField(jiraService, "slackWorkspaceUrl", "https://workspace.slack.com");

        String slackAssigneeId = "U12345";
        String email = "assignee@example.com";
        String jiraAccountId = "jira-account-123";

        Issue issue = createIssue(slackAssigneeId);
        User slackUser = createSlackUser(slackAssigneeId, email);
        JiraUserResponse jiraUserResponse = new JiraUserResponse(jiraAccountId, email, true, "ralph", "1234", "ralph", "");
        JiraIssueResponse expectedResponse = new JiraIssueResponse("10001", "PROJ-1", "https://jira.com/browse/PROJ-1");

        given(jiraApiClient.searchUsersWithQuery(email, 0, 1))
                .willReturn(List.of(jiraUserResponse));
        given(jiraApiClient.createIssue(any(CreateJiraIssueRequest.class)))
                .willReturn(expectedResponse);
        given(slackApiService.fetchUser(slackUser.getId()))
                .willReturn(slackUser);

        // when
        JiraIssueResponse result = jiraService.createIssue(issue);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(jiraApiClient).searchUsersWithQuery(email, 0, 1);
        verify(jiraApiClient).createIssue(any(CreateJiraIssueRequest.class));
    }

    @Test
    @DisplayName("슬랙 담당자를 찾을 수 없으면 예외가 발생한다")
    void createIssue_slackUserNotFound() {
        // given
        String slackAssigneeId = "U12345";
        Issue issue = createIssue(slackAssigneeId);
        User otherSlackUser = createSlackUser("U99999", "other@example.com");

        given(slackApiService.fetchUser(otherSlackUser.getId()))
                .willReturn(otherSlackUser);

        // when & then
        assertThatThrownBy(() -> jiraService.createIssue(issue))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Jira 사용자를 찾을 수 없으면 예외가 발생한다")
    void createIssue_jiraUserNotFound() {
        // given
        ReflectionTestUtils.setField(jiraService, "projectKey", "PROJ");

        String slackAssigneeId = "U12345";
        String email = "assignee@example.com";

        Issue issue = createIssue(slackAssigneeId);
        User slackUser = createSlackUser(slackAssigneeId, email);

        given(jiraApiClient.searchUsersWithQuery(email, 0, 1))
                .willReturn(List.of());
        given(slackApiService.fetchUser(slackUser.getId()))
                .willReturn(slackUser);

        // when & then
        assertThatThrownBy(() -> jiraService.createIssue(issue))
                .isInstanceOf(NotFoundException.class);
    }

    private Issue createIssue(String slackAssigneeId) {
        return Issue.builder()
                .id(1L)
                .status(IssueStatus.OPEN)
                .environment(Environment.PROD)
                .title("이슈 제목")
                .description("이슈 설명")
                .userEmail("user@example.com")
                .applicationTypes(Set.of(ApplicationType.IOS))
                .courseTypes(Set.of(CourseType.ALL))
                .slackContext(Issue.SlackContext.builder()
                        .channelId("C12345")
                        .messageTs("1234567890.123456")
                        .reporterId("U11111")
                        .assigneeId(slackAssigneeId)
                        .assignedUsergroupIds(Set.of("1"))
                        .build())
                .jiraContext(Issue.JiraContext.builder()
                        .issueKey(null)
                        .issueUrl(null)
                        .assigneeId(null)
                        .build())
                .build();
    }

    private User createSlackUser(String userId, String email) {
        Profile profile = new Profile();
        profile.setEmail(email);
        profile.setDisplayName("John Doe");

        User user = new User();
        user.setId(userId);
        user.setProfile(profile);

        return user;
    }
}