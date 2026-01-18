package com.teuida.jikimi.jira.client.dto.response;

import java.util.List;

public record JiraTransitionsResponse(List<TransitionDetail> transitions) {
    public record TransitionDetail(String id,
                                   String name,
                                   To to,
                                   Boolean hasScreen,
                                   Boolean isGlobal,
                                   Boolean isInitial,
                                   Boolean isAvailable,
                                   Boolean isConditional) {
    }

    public record To(String self,
                     String description,
                     String iconUrl,
                     String name,
                     String id,
                     StatusCategory statusCategory) {
    }

    public record StatusCategory(String self,
                                 Long id,
                                 String key,
                                 String colorName,
                                 String name) {
    }
}
