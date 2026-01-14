package com.teuida.jikimi.jira.service;

import com.slack.api.model.User;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JiraService {

    private final JiraApiClient jiraApiClient;
    @Value("${jira.project-key}")
    private String projectKey;
    @Value("${slack.workspace-url}")
    private String slackWorkspaceUrl;

    /**
     * 슬랙 이메일을 사용하여 jira 유저를 조회하고 해당 유저의 티켓을 생성 합니다.
     *
     * @param issue
     * @return
     */
    public JiraIssueResponse createIssue(Issue issue, List<User> slackUsers) {
        // 슬랙 사용자 조회
        User findSlackUser = slackUsers.stream()
                .filter(slackUser -> issue.isEqualsSlackAssigneeId(slackUser.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("이슈 담당자로 지정된 슬랙 사용자를 찾을 수 없습니다."));

        // 회원 조회
        List<JiraUserResponse> jiraUsersResponse = jiraApiClient.searchUsersWithQuery(findSlackUser.getProfile().getEmail(), 0,
                1);
        if (jiraUsersResponse.isEmpty()) {
            throw new NotFoundException("슬랙 이메일로 가입된 Jira Cloud 사용자를 찾을 수 없습니다.");
        }

        JiraUserResponse jiraUserResponse = jiraUsersResponse.get(0);

        // 이슈 생성 요청 객체 생성
        CreateJiraIssueRequest createJiraIssueRequest = CreateJiraIssueRequest.from(issue, slackWorkspaceUrl, projectKey,
                jiraUserResponse.accountId());

        // 이슈 생성
        return jiraApiClient.createIssue(createJiraIssueRequest);
    }
}
