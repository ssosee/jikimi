package com.teuida.jikimi.jira.service;

import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.UserMappingService;
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.request.TransitionJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraTransitionsResponse.TransitionDetail;
import com.teuida.jikimi.jira.service.dto.JiraIssue;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraService {

    private final JiraApiClient jiraApiClient;
    private final UserMappingService userMappingService;

    @Value("${jira.project-key}")
    private String jiraProjectKey;

    @Value("${slack.workspace-url}")
    private String slackWorkspaceUrl;

    /**
     * 슬랙에서 회원의 이메일 정보를 사용하여 jira 회원을 조회하고 해당 회원으로 티켓을 생성 합니다.
     *
     * @param issue
     * @return
     */
    public JiraIssue createIssue(Issue issue, String jiraIssueTypeId, String jiraPriorityId) {
        // 슬랙 사용자 조회
        String slackAssigneeId = issue.getSlackContext().getAssigneeId();

        // 슬랙 사용자로 매핑된 지라 사용자 조회
        String jiraAccountId = userMappingService.getJiraAccountId(slackAssigneeId);

        // 이슈 생성 요청 객체 생성
        CreateJiraIssueRequest createJiraIssueRequest = CreateJiraIssueRequest.from(
                issue,
                slackWorkspaceUrl,
                jiraProjectKey,
                jiraAccountId,
                jiraIssueTypeId,
                jiraPriorityId
        );

        // 이슈 생성
        JiraIssueResponse jiraIssueResponse = jiraApiClient.createIssue(createJiraIssueRequest);

        return JiraIssue.of(jiraIssueResponse, jiraAccountId);
    }

    public void completeIssue(List<TransitionDetail> transitions, String jiraIssueKey) {
        TransitionDetail completedTransitionDetail = transitions.stream()
                .filter(transitionDetail -> transitionDetail.name().equals("완료"))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Jira 티켓에 '완료' 상태가 존재하지 않습니다."));

        // 완료로 전환
        jiraApiClient.transitionIssue(jiraIssueKey, TransitionJiraIssueRequest.of(completedTransitionDetail.id()));
    }
}
