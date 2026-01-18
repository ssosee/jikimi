package com.teuida.jikimi.jira.client.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.domain.issue.model.Issue;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;

public record CreateJiraIssueRequest(Fields fields) {

    public static CreateJiraIssueRequest from(Issue issue, String slackWorkspaceUrl, String projectKey, String jiraAccountId,
                                              String issueTypeId, String priorityId) {
        return new CreateJiraIssueRequest(
                Fields.builder()
                        .project(new ProjectRef(projectKey))
                        .issuetype(new IssueTypeRef(issueTypeId))
                        .summary(issue.getTitle())
                        .description(createDescription(issue, slackWorkspaceUrl))
                        .assignee(new UserRef(jiraAccountId))
                        .reporter(new UserRef(jiraAccountId))
                        .priority(new PriorityRef(priorityId))
                        .build()
        );
    }

    private static AdfDocument createDescription(Issue issue, String slackWorkspaceUrl) {
        return new AdfDocument(List.of(
                // 환경
                createHeading("환경"),
                createParagraph(issue.getEnvironment().name()),

                // 애플리케이션
                createHeading("애플리케이션"),
                createParagraph(formatApplicationTypes(issue.getApplicationTypes())),

                // 코스
                createHeading("코스"),
                createParagraph(formatCourseTypes(issue.getCourseTypes())),

                // 제보자
                createHeading("제보자"),
                createParagraph(issue.getUserEmail() != null ? issue.getUserEmail() : "-"),

                // 이슈 내용
                createHeading("이슈 내용"),
                createParagraph(issue.getDescription()),

                // 관련 스레드
                createHeading("관련 스레드"),
                createParagraphWithLink(issue.getSlackThreadUrl(slackWorkspaceUrl), "Slack 스레드 바로가기")
        ));
    }

    private static AdfNode createHeading(String text) {
        return new AdfNode("heading",
                List.of(new AdfContent(text)),
                new AdfNodeAttrs(3));
    }

    private static AdfNode createParagraph(String text) {
        return new AdfNode("paragraph",
                List.of(new AdfContent(text)), null);
    }

    private static AdfNode createParagraphWithLink(String url, String displayText) {
        return new AdfNode("paragraph",
                List.of(AdfContent.link(url, displayText)),
                null);
    }

    private static String formatApplicationTypes(Set<ApplicationType> types) {
        return types.stream()
                .map(ApplicationType::getDisplayNameForJira)
                .collect(Collectors.joining(", "));
    }

    private static String formatCourseTypes(Set<CourseType> types) {
        return types.stream()
                .map(CourseType::getDisplayNameForJira)
                .collect(Collectors.joining(", "));
    }

    @Builder
    public record Fields(ProjectRef project,
                         IssueTypeRef issuetype,
                         String summary,
                         AdfDocument description,
                         UserRef assignee,
                         UserRef reporter,
                         PriorityRef priority) {
    }

    public record ProjectRef(String key) {
    }

    public record IssueTypeRef(String id) {
    }

    public record UserRef(String accountId) {
    }

    public record AdfDocument(
            String type,
            int version,
            List<AdfNode> content) {
        public AdfDocument(List<AdfNode> content) {
            this("doc", 1, content);
        }
    }

    public record AdfNode(
            String type,
            List<AdfContent> content,
            @JsonInclude(JsonInclude.Include.NON_NULL)
            AdfNodeAttrs attrs) {
        public AdfNode(List<AdfContent> content) {
            this("paragraph", content, null);
        }
    }

    public record AdfNodeAttrs(int level) {

    }  // heading level용

    public record AdfContent(String type,
                             String text,
                             @JsonInclude(JsonInclude.Include.NON_NULL) List<AdfMark> marks
    ) {
        public AdfContent(String text) {
            if (text.isBlank()) {
                throw new IllegalArgumentException("Text cannot be blank.");
            }
            this("text", text, null);
        }

        public static AdfContent link(String url, String displayText) {
            return new AdfContent(
                    "text",
                    displayText,
                    List.of(new AdfMark("link", new AdfMarkAttrs(url)))
            );
        }
    }

    public record PriorityRef(String id) {
    }

    public record AdfMark(String type, AdfMarkAttrs attrs) {
    }

    public record AdfMarkAttrs(String href) {
    }
}