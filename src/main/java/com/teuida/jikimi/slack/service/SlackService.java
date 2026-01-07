package com.teuida.jikimi.slack.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.response.usergroups.UsergroupsListResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.User;
import com.slack.api.model.Usergroup;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SlackService {

    public List<Usergroup> fetchUserGroups(MethodsClient client, String botToken) {
        try {
            // User Group 목록을 가져와서 OptionObject 리스트로 반환
            UsergroupsListResponse response = client.usergroupsList(r -> r.token(botToken));

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

    public List<User> fetchUsers(MethodsClient client, String botToken) {
        try {
            // User 목록을 가져와서 반환
            UsersListResponse response = client.usersList(r -> r.token(botToken));

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
}
