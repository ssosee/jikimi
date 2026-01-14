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
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
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
    private JiraApiClient jiraApiClient;

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

        // when
        JiraIssueResponse result = jiraService.createIssue(issue, List.of(slackUser));

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

        // when & then
        assertThatThrownBy(() -> jiraService.createIssue(issue, List.of(otherSlackUser)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("이슈 담당자로 지정된 슬랙 사용자를 찾을 수 없습니다.");
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

        // when & then
        assertThatThrownBy(() -> jiraService.createIssue(issue, List.of(slackUser)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("슬랙 이메일로 가입된 Jira Cloud 사용자를 찾을 수 없습니다.");
    }

    private Issue createIssue(String slackAssigneeId) {
        return new Issue(
                1L,
                "1234567890.123456",
                "C12345",
                "U11111",
                slackAssigneeId,
                Set.of("1"),
                Environment.PROD,
                IssueStatus.OPEN,
                Set.of(ApplicationType.IOS),
                Set.of(CourseType.ALL),
                "이슈 제목",
                "이슈 설명",
                "user@example.com",
                null,
                null,
                null
        );
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