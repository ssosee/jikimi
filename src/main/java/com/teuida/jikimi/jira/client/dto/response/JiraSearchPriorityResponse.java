package com.teuida.jikimi.jira.client.dto.response;

import java.util.List;

public record JiraSearchPriorityResponse(Long maxResults,
                                         Long startAt,
                                         Long total,
                                         Boolean isLast,
                                         List<Value>
                                         values
) {

    public record Value(String self,
                        String statusColor,
                        String description,
                        String iconUrl,
                        String name,
                        String id,
                        Boolean isDefault
    ) {
    }
}
