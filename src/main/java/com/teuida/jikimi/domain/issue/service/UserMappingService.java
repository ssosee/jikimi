package com.teuida.jikimi.domain.issue.service;

import com.slack.api.model.User;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.entity.UserMappingEntity;
import com.teuida.jikimi.domain.issue.repository.UserMappingEntityRepository;
import com.teuida.jikimi.jira.client.JiraApiClient;
import com.teuida.jikimi.jira.client.dto.response.JiraUserResponse;
import com.teuida.jikimi.slack.service.SlackApiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserMappingService {

    private final UserMappingEntityRepository userMappingEntityRepository;
    private final JiraApiClient jiraApiClient;
    private final SlackApiService slackApiService;

    @Transactional
    public String getJiraAccountId(String slackAssigneeId) {
        // 슬랙 사용자 ID로 유저 매핑 조회 후 Jira 계정 ID 반환
        return userMappingEntityRepository.findBySlackUserId(slackAssigneeId)
                .map(UserMappingEntity::getJiraAccountId)
                .orElseGet(() -> createMappingFromApi(slackAssigneeId));
    }

    private String createMappingFromApi(String slackAssigneeId) {
        // 슬랙 유저 조회
        User findSlackUser = slackApiService.fetchUser(slackAssigneeId);
        String slackEmail = findSlackUser.getProfile().getEmail();

        if (!StringUtils.hasText(slackEmail)) {
            throw new IllegalArgumentException("Slack 사용자 이메일이 존재하지 않습니다.");
        }

        // Jira API 호출하여 슬랙 이메일로 사용자 조회
        List<JiraUserResponse> jiraUsersResponse = jiraApiClient.searchUsers(slackEmail, null, 0, 1);

        // 존재하지 않는 경우 예외 처리
        if (jiraUsersResponse.isEmpty()) {
            throw new NotFoundException(String.format("이메일(%s)로 가입된 Jira 사용자를 찾을 수 없습니다.", slackEmail));
        }

        JiraUserResponse jiraUser = jiraUsersResponse.getFirst();

        // DB에 저장
        UserMappingEntity userMappingEntity = UserMappingEntity.create(jiraUser, findSlackUser);
        userMappingEntityRepository.save(userMappingEntity);

        // Jira 계정 ID 반환
        return userMappingEntity.getJiraAccountId();
    }
}
