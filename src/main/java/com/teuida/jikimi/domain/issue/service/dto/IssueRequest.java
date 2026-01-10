package com.teuida.jikimi.domain.issue.service.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class IssueRequest {
    private final String requestUserId;

    public IssueRequest(String requestUserId) {
        this.requestUserId = requestUserId;
    }
}
