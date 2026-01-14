package com.teuida.jikimi.jira.service;

import com.teuida.jikimi.jira.dto.JiraUser;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class JiraApiService {
    private final RestTemplate jiraRestTemplate;
    @Value("${jira.url}")
    private String jiraUrl;

    /**
     * GDPR 준수: 이메일로 accountId 조회
     */
    public List<JiraUser> getAccountIdByEmail(String email) {
        String url = String.format("%s/rest/api/3/user/search?query=%s&startAt=0&maxResults=1",
                jiraUrl,
                URLEncoder.encode(email, StandardCharsets.UTF_8));

        return jiraRestTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<JiraUser>>() {
                    @Override
                    public Type getType() {
                        return super.getType();
                    }
                }).getBody();
    }
}
