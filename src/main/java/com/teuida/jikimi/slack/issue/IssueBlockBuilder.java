package com.teuida.jikimi.slack.issue;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.asOptions;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static com.slack.api.model.block.element.BlockElements.overflowMenu;
import static com.slack.api.model.block.element.BlockElements.usersSelect;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_MORE_OPTIONS;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SOLVE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_DELETE_ISSUE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_SOLVE;

import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.element.ButtonElement;
import com.slack.api.model.block.element.OverflowMenuElement;
import com.slack.api.model.block.element.UsersSelectElement;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.model.Issue;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

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
        if (issue.status() != IssueStatus.CLOSED) {
            blocks.addAll(buildActionButtons(issue));
        }

        return blocks;
    }

    // ========== 헤더 ==========
    private static HeaderBlock buildHeader(Issue issue) {
        return header(h -> h.text(plainText(pt -> pt
                .emoji(true)
                .text(String.format(":alert: %s :rainbow-right:", issue.title()))
        )));
    }

    // ========== 메타데이터 필드 ==========
    private static List<LayoutBlock> buildMetadataFields(Issue issue) {
        return List.of(
                buildEnvironmentAndApplicationRow(issue),
                buildCourseAndReporterRow(issue),
                buildTeamAndAssigneeRow(issue),
                buildEmailRow(issue)
        );
    }

    private static SectionBlock buildEnvironmentAndApplicationRow(Issue issue) {
        String applications = issue.applicationTypes().stream()
                .map(ApplicationType::getDisplayName)
                .collect(Collectors.joining(", "));

        return section(s -> s.fields(List.of(
                markdownText("*환경:* `" + issue.environment() + "`"),
                markdownText("*애플리케이션:* " + applications)
        )));
    }

    private static SectionBlock buildCourseAndReporterRow(Issue issue) {
        String courses = issue.courseTypes().stream()
                .map(CourseType::getDisplayName)
                .collect(Collectors.joining(", "));

        return section(s -> s.fields(List.of(
                markdownText("*코스:* " + courses),
                markdownText("*제보자:* <@" + issue.slackReporterId() + ">")
        )));
    }

    private static SectionBlock buildTeamAndAssigneeRow(Issue issue) {
        String usergroups = issue.slackAssignedUsergroupIds().stream()
                .map(id -> "<!subteam^" + id + ">")
                .collect(Collectors.joining(", "));

        String assignee = issue.slackAssigneeId() != null
                ? "<@" + issue.slackAssigneeId() + ">"
                : "-";

        return section(s -> s.fields(List.of(
                markdownText("*담당팀:* " + usergroups),
                markdownText("*담당자:* " + assignee)
        )));
    }

    private static SectionBlock buildEmailRow(Issue issue) {
        String email = issue.userEmail() != null ? issue.userEmail() : "-";
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
                section(s -> s.text(markdownText(issue.description())))
        );
    }

    private static SectionBlock buildImageUploadGuide() {
        return section(s -> s.text(
                markdownText("📸 필요한 사진 또는 영상은 스레드 댓글에 업로드 해주세요.")
        ));
    }

    // ========== 액션 버튼 ==========
    private static List<LayoutBlock> buildActionButtons(Issue issue) {
        if (StringUtils.hasText(issue.slackAssigneeId())) {
            return buildAssignedActions(issue);
        } else {
            return buildUnassignedActions(issue);
        }
    }

    private static List<LayoutBlock> buildAssignedActions(Issue issue) {
        return List.of(
                header(h -> h.text(plainText(pt -> pt
                        .text("👇 이슈에 대해 다음 작업을 선택하세요.")
                ))),
                actions(a -> a
                        .blockId(String.valueOf(issue.id()))
                        .elements(asElements(
                                createTicketButton(),
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
                        .blockId(String.valueOf(issue.id()))
                        .elements(asElements(
                                assignToMeButton(),
                                assigneeSelector(),
                                moreOptionsMenu()
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

    private static OverflowMenuElement moreOptionsMenu() {
        return overflowMenu(o -> o
                .actionId(ACTION_MORE_OPTIONS)
                .options(asOptions(
                        option(opt -> opt
                                .text(plainText(":delete: 삭제"))
                                .value(VALUE_DELETE_ISSUE)
                        )
                ))
        );
    }
}
