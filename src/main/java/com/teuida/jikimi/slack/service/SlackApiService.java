package com.teuida.jikimi.slack.service;

import com.slack.api.Slack;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.User;
import com.slack.api.model.Usergroup;
import com.teuida.jikimi.domain.exception.NotFoundException;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackApiService {
    private final Slack slack;

    @Value("${slack.bot-token}")
    private String botToken;

    public List<Usergroup> fetchUserGroups() {
        try {
            // User Group 목록을 가져와서 OptionObject 리스트로 반환
            UsergroupsListResponse response = slack.methods()
                    .usergroupsList(builder -> builder.token(botToken));

            if (!response.isOk()) {
                log.error("Failed to fetch user groups: {}", response.getError());
                return Collections.emptyList();
            }

            return response.getUsergroups();

        } catch (Exception e) {
            log.error("Slack API Error", e);
            return Collections.emptyList();
        }
    }

    public List<User> fetchUsers() {
        try {
            // User 목록을 가져와서 반환
            UsersListResponse response = slack.methods()
                    .usersList(builder -> builder.token(botToken));

            if (!response.isOk()) {
                log.error("Failed to fetch users: {}", response.getError());
                return Collections.emptyList();
            }

            return response.getMembers();

        } catch (Exception e) {
            log.error("Slack API Error", e);
            return Collections.emptyList();
        }
    }

    public User fetchUser(String slackUserId) {
        try {
            // User 조회
            UsersInfoResponse response = slack.methods().usersInfo(builder -> builder
                    .token(botToken)
                    .user(slackUserId));

            if (!response.isOk() || response.getUser() == null) {
                throw new NotFoundException(
                        String.format("Slack 사용자를 찾을 수 없습니다. slackUserId=%s, error=%s", slackUserId, response.getError())
                );
            }

            return response.getUser();

        } catch (Exception e) {
            log.error("Slack API Error. slackUserId={}", slackUserId, e);
            throw new RuntimeException("Slack 사용자 조회 실패", e);
        }
    }
}
