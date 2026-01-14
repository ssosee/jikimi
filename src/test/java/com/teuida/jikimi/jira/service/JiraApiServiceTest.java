package com.teuida.jikimi.jira.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.teuida.jikimi.jira.dto.JiraUser;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JiraApiServiceTest {
    @Autowired
    JiraApiService jiraApiService;

    @Disabled
    @Test
    @DisplayName("이메일로 Jira accountId 조회 API 를 호출한다.")
    void test() {
        // given
        String email = "ralph@teuda.net";
        // when
        List<JiraUser> findJiraUsers = jiraApiService.getAccountIdByEmail(email);
        // then
        assertNotNull(findJiraUsers);
    }
}