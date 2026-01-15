package com.teuida.jikimi.jira.service;

import com.slack.api.model.User;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.UserMappingService;
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JiraService {

    private final JiraApiClient jiraApiClient;
    private final UserMappingService userMappingService;

    @Value("${jira.project-key}")
    private String projectKey;
    @Value("${slack.workspace-url}")
    private String slackWorkspaceUrl;

    /**
     * 슬랙에서 회원의 이메일 정보를 사용하여 jira 회원을 조회하고 해당 회원으로 티켓을 생성 합니다.
     *
     * @param issue
     * @return
     */
    public JiraIssueResponse createIssue(Issue issue) {
        // 슬랙 사용자 조회
        String slackAssigneeId = issue.getSlackContext().getAssigneeId();

        // 슬랙 사용자로 매핑된 지라 사용자 조회
        String jiraAccountId = userMappingService.getJiraAccountId(slackAssigneeId);

        // 이슈 생성 요청 객체 생성
        CreateJiraIssueRequest createJiraIssueRequest = CreateJiraIssueRequest.from(
                issue,
                slackWorkspaceUrl,
                projectKey,
                jiraAccountId
        );

        // 이슈 생성
        return jiraApiClient.createIssue(createJiraIssueRequest);
    }

    private User findSlackUser(List<User> slackUsers, String slackUserId) {
        return slackUsers.stream()
                .filter(user -> user.getId().equals(slackUserId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Slack 사용자를 찾을 수 없습니다."));
    }
}
