package com.teuida.jikimi.jira.client;

import com.teuida.jikimi.jira.client.dto.request.CreateJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.request.TransitionJiraIssueRequest;
import com.teuida.jikimi.jira.client.dto.response.JiraIssueResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraTransitionsResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
import com.teuida.jikimi.jira.config.JiraFeignConfig;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>참고1</p>
 * <a href="https://developer.atlassian.com/cloud/jira/platform/rest/v3/intro/">official jira api docs</a>
 * <p>참고2</p>
 * <a href="https://jira-api.apidog.io/">jira api dog</a>
 */
@FeignClient(
        name = "jira-api",
        url = "${jira.url}",
        configuration = JiraFeignConfig.class
)
public interface JiraApiClient {

    /**
     * <a
     * href="https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-user-search/#api-rest-api-3-user-search-query-get">고급
     * 검색 (정확한 매칭 필요 시) GDPR 준수: 이메일로 정확한 accountId 조회</a>
     */
    @GetMapping("/rest/api/3/user/search/query")
    List<JiraUserResponse> searchUsersWithQuery(
            @RequestParam("query") String query,
            @RequestParam(value = "startAt", defaultValue = "0") int startAt,
            @RequestParam(value = "maxResults", defaultValue = "50") int maxResults
    );

    /**
     * <a
     * href="https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-user-search/#api-rest-api-3-user-search-query-get">단순
     * 검색 (자동완성, 추천 등) 사용자 선택 UI에서 이름 일부로 검색</a>
     */
    @GetMapping("/rest/api/3/user/search")
    List<JiraUserResponse> searchUsers(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "accountId", required = false) String accountId,
            @RequestParam(value = "startAt", defaultValue = "0") int startAt,
            @RequestParam(value = "maxResults", defaultValue = "50") int maxResults
    );

    /**
     * <a href="https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-post">티켓
     * 생성</a>
     */
    @PostMapping("/rest/api/3/issue")
    JiraIssueResponse createIssue(@RequestBody CreateJiraIssueRequest request);

    // 프로젝트의 Issue Type 조회
    @GetMapping("/rest/api/3/project/{projectKey}")
    JiraProjectResponse getProject(@PathVariable String projectKey);

    // 특정 프로젝트에서 사용 가능한 우선순위만 조회
    @GetMapping("/rest/api/3/priority/search")
    JiraSearchPriorityResponse searchPriorities(@RequestParam(required = false) String projectId);

    // 가능한 전환 목록 조회
    @GetMapping("/rest/api/3/issue/{issueIdOrKey}/transitions")
    JiraTransitionsResponse getTransitions(@PathVariable String issueIdOrKey);

    // 티켓 상태 변경
    @PostMapping("/rest/api/3/issue/{issueIdOrKey}/transitions")
    void transitionIssue(@PathVariable String issueIdOrKey, @RequestBody TransitionJiraIssueRequest request);

}
