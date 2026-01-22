package com.teuida.jikimi.slack.issue;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static com.slack.api.model.block.element.BlockElements.usersSelect;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SOLVE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_SOLVE;

import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.UsersSelectElement;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.model.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class IssueBlockBuilder {

    public static List<LayoutBlock> buildIssueBlocks(Issue issue) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 헤더 & 메타데이터
        blocks.add(buildHeader(issue));
        blocks.add(divider());
        blocks.addAll(buildMetadataFields(issue));
        blocks.add(divider());

        // 이슈 내용
        blocks.addAll(buildDescriptionSection(issue));
        blocks.add(divider());
        blocks.add(buildImageUploadGuide());

        // 액션 버튼 (상태에 따라)
        if (issue.getStatus() != IssueStatus.CLOSED) {
            blocks.addAll(buildActionButtons(issue));
        }

        return blocks;
    }

    public static List<LayoutBlock> buildSimilarityIssueBlocks(List<Issue> topSimilarityIssues, String slackWorkspaceUrl) {
        List<LayoutBlock> blocks = new ArrayList<>();
        blocks.add(buildHeader(String.format("🔎 *유사한 이슈 %d개를 조회했습니다.*", topSimilarityIssues.size())));
        blocks.add(divider());

        // 각 이슈
        for (Issue topSimilarityIssue : topSimilarityIssues) {
            String title = topSimilarityIssue.getTitle();
            String threadUrl = topSimilarityIssue.getSlackContext().getThreadUrl(slackWorkspaceUrl);

            blocks.add(SectionBlock.builder()
                    .text(MarkdownTextObject.builder()
                            .text(String.format("* <%s|%s>", threadUrl, title))
                            .build())
                    .build());
        }

        return blocks;
    }

    private static HeaderBlock buildHeader(String text) {
        return header(h -> h.text(plainText(pt -> pt
                .emoji(true)
                .text(text)
        )));
    }

    // ========== 헤더 ==========
    private static HeaderBlock buildHeader(Issue issue) {
        return header(h -> h.text(plainText(pt -> pt
                .emoji(true)
                .text(String.format(":alert: %s :rainbow-right:", issue.getTitle()))
        )));
    }

    // ========== 메타데이터 필드 ==========
    private static List<LayoutBlock> buildMetadataFields(Issue issue) {
        return List.of(
                buildEnvironmentAndApplicationRow(issue),
                buildCourseAndReporterRow(issue),
                buildTeamAndAssigneeRow(issue.getSlackContext()),
                buildEmailRow(issue)
        );
    }

    private static SectionBlock buildEnvironmentAndApplicationRow(Issue issue) {
        String applications = issue.getApplicationTypes().stream()
                .map(ApplicationType::getDisplayNameForSlack)
                .collect(Collectors.joining(", "));

        return section(s -> s.fields(List.of(
                markdownText("*환경:* `" + issue.getEnvironment() + "`"),
                markdownText("*애플리케이션:* " + applications)
        )));
    }

    private static SectionBlock buildCourseAndReporterRow(Issue issue) {
        String courses = issue.getCourseTypes().stream()
                .map(CourseType::getDisplayNameForSlack)
                .collect(Collectors.joining(", "));

        return section(s -> s.fields(List.of(
                markdownText("*코스:* " + courses),
                markdownText("*제보자:* <@" + issue.getSlackContext().getReporterId() + ">")
        )));
    }

    private static SectionBlock buildTeamAndAssigneeRow(Issue.SlackContext slackContext) {
        String usergroups = slackContext.getAssignedUsergroupIds().stream()
                .map(id -> "<!subteam^" + id + ">")
                .collect(Collectors.joining(", "));

        String assignee = slackContext.getAssigneeId() != null
                ? "<@" + slackContext.getAssigneeId() + ">"
                : "-";

        return section(s -> s.fields(List.of(
                markdownText("*담당팀:* " + usergroups),
                markdownText("*담당자:* " + assignee)
        )));
    }

    private static SectionBlock buildEmailRow(Issue issue) {
        String email = issue.getUserEmail() != null ? issue.getUserEmail() : "-";
        return section(s -> s.fields(List.of(
                markdownText("*이메일:* " + email)
        )));
    }

    // ========== 이슈 내용 ==========
    private static List<LayoutBlock> buildDescriptionSection(Issue issue) {
        return List.of(
                header(h -> h.text(plainText(pt -> pt
                        .emoji(true)
                        .text("✏️ 이슈 내용")
                ))),
                section(s -> s.text(markdownText(issue.getDescription())))
        );
    }

    private static SectionBlock buildImageUploadGuide() {
        return section(s -> s.text(
                markdownText("📸 필요한 사진 또는 영상은 스레드 댓글에 업로드 해주세요.")
        ));
    }

    // ========== 액션 버튼 ==========
    private static List<LayoutBlock> buildActionButtons(Issue issue) {
        if (issue.isFullyAssigned()) {
            return buildFullyAssignedActions(issue);
        }

        if (issue.isOnlySlackAssigned()) {
            return buildOnlySlackAssignedActions(issue);
        }

        return buildUnassignedActions(issue);
    }

    private static List<LayoutBlock> buildOnlySlackAssignedActions(Issue issue) {
        return List.of(
                header(h -> h.text(plainText(pt -> pt
                        .text("👇 이슈에 대해 다음 작업을 선택하세요.")
                ))),
                actions(a -> a
                        .blockId(String.valueOf(issue.getId()))
                        .elements(asElements(
                                createTicketButton(),
                                solveButton()
                        ))
                )
        );
    }

    private static List<LayoutBlock> buildFullyAssignedActions(Issue issue) {
        return List.of(
                header(h -> h.text(plainText(pt -> pt
                        .text("👇 이슈에 대해 다음 작업을 선택하세요.")
                ))),
                actions(a -> a
                        .blockId(String.valueOf(issue.getId()))
                        .elements(asElements(
                                solveButton()
                        ))
                )
        );
    }

    private static List<LayoutBlock> buildUnassignedActions(Issue issue) {
        return List.of(
                header(h -> h.text(plainText(pt -> pt
                        .text("👇 아래 버튼을 눌러 담당자를 지정하세요.")
                ))),
                actions(a -> a
                        .blockId(String.valueOf(issue.getId()))
                        .elements(asElements(
                                assignToMeButton(),
                                assigneeSelector()
                        ))
                )
        );
    }

    // ========== 버튼 요소들 ==========
    private static ButtonElement createTicketButton() {
        return button(b -> b
                .text(plainText(":nyancat_big: 티켓 생성"))
                .value(VALUE_CREATE_TICKET)
                .actionId(ACTION_CREATE_TICKET)
        );
    }

    private static ButtonElement solveButton() {
        return button(b -> b
                .text(plainText("🎉 해결 완료"))
                .value(VALUE_SOLVE)
                .actionId(ACTION_SOLVE)
        );
    }

    private static ButtonElement assignToMeButton() {
        return button(b -> b
                .text(plainText("나에게 할당"))
                .value(VALUE_ASSIGN_TO_ME)
                .actionId(ACTION_ASSIGN_TO_ME)
        );
    }

    private static UsersSelectElement assigneeSelector() {
        return usersSelect(u -> u
                .placeholder(plainText("할당할 팀원 ..."))
                .actionId(ACTION_SELECT_ASSIGNEE)
        );
    }
}
