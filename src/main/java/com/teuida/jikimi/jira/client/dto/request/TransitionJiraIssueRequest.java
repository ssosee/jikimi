package com.teuida.jikimi.jira.client.dto.request;

public record TransitionJiraIssueRequest(Transition transition) {

    public static TransitionJiraIssueRequest of(String transitionId) {
        return new TransitionJiraIssueRequest(new Transition(transitionId));
    }

    public record Transition(String id) {
    }
}
