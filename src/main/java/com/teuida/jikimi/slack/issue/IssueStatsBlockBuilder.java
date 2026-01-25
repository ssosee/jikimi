package com.teuida.jikimi.slack.issue;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_PUBLISH_STATS;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_TAG_ASSIGNEES;

import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.element.BlockElement;
import com.slack.api.model.block.element.ButtonElement;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.dto.NotSolvedIssueRow;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class IssueStatsBlockBuilder {

    public static List<LayoutBlock> buildStatsBlocks(IssueStatsRow result, String workspaceUrl, String buttonValue) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 헤더
        blocks.add(buildHeader(result));
        blocks.add(divider());

        // 전체 통계
        blocks.add(buildTotalSection(result));

        // 상태별 통계
        blocks.addAll(buildStatusSections(result));

        // 미해결 이슈 목록
        if (!result.getNotSolvedIssueRows().isEmpty()) {
            blocks.add(divider());
            blocks.addAll(buildOpenIssuesList(result.getNotSolvedIssueRows(), workspaceUrl));
        }

        // 버튼 추가 (미해결 이슈가 있을 경우에만)
        if (!result.getNotSolvedIssueRows().isEmpty()) {
            blocks.add(divider());
            blocks.add(buildActionButtons(buttonValue));
        }

        return blocks;
    }

    public static List<LayoutBlock> buildStatsBlocksWithTagButtonOnly(
            IssueStatsRow result, String workspaceUrl, String buttonValue) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 헤더
        blocks.add(buildHeader(result));
        blocks.add(divider());

        // 전체 통계
        blocks.add(buildTotalSection(result));

        // 상태별 통계
        blocks.addAll(buildStatusSections(result));

        // 미해결 이슈 목록
        if (!result.getNotSolvedIssueRows().isEmpty()) {
            blocks.add(divider());
            blocks.addAll(buildOpenIssuesList(result.getNotSolvedIssueRows(), workspaceUrl));
        }

        // 담당자 태그 버튼만 추가 (미해결 이슈가 있을 경우에만)
        if (!result.getNotSolvedIssueRows().isEmpty()) {
            blocks.add(divider());
            blocks.add(buildTagAssigneeButton(buttonValue));
        }

        return blocks;
    }

    public static List<LayoutBlock> buildEmptyBlocks(String commandText) {
        return List.of(
                section(s -> s.text(markdownText(String.format("😅 조건에 맞는 이슈가 없습니다. (사용한 커맨드: %s)", commandText))))
        );
    }

    private static LayoutBlock buildHeader(IssueStatsRow result) {
        String headerText = ":bar_chart: 이슈 현황 (" + result.getPeriodDisplayName() + ")";
        return header(h -> h.text(plainText(pt -> pt.emoji(true).text(headerText))));
    }

    private static LayoutBlock buildTotalSection(IssueStatsRow result) {
        return section(s -> s.text(markdownText(
                String.format("*전체:* %d건", result.getTotalCount())
        )));
    }

    private static List<LayoutBlock> buildStatusSections(IssueStatsRow result) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 완료 (CLOSED)
        long closedCount = result.getCount(IssueStatus.CLOSED);
        double closedPercent = result.getPercentage(IssueStatus.CLOSED);
        blocks.add(section(s -> s.text(markdownText(
                String.format("- 🟩 %s: %d건 (%.0f%%)", IssueStatus.CLOSED.getDisplayName(), closedCount, closedPercent)
        ))));

        // 진행중 (IN_PROGRESS)
        long inProgressCount = result.getCount(IssueStatus.IN_PROGRESS);
        double inProgressPercent = result.getPercentage(IssueStatus.IN_PROGRESS);
        blocks.add(section(s -> s.text(markdownText(
                String.format("- 🟦 %s: %d건 (%.0f%%)", IssueStatus.IN_PROGRESS.getDisplayName(), inProgressCount,
                        inProgressPercent)
        ))));

        // 접수 (OPEN)
        long openCount = result.getCount(IssueStatus.OPEN);
        double openPercent = result.getPercentage(IssueStatus.OPEN);
        blocks.add(section(s -> s.text(markdownText(
                String.format("- 🟥 %s: %d건 (%.0f%%)", IssueStatus.OPEN.getDisplayName(), openCount, openPercent)
        ))));

        return blocks;
    }

    private static List<LayoutBlock> buildOpenIssuesList(List<NotSolvedIssueRow> openIssues, String workspaceUrl) {
        List<LayoutBlock> blocks = new ArrayList<>();

        blocks.add(section(s -> s.text(markdownText(":link: *미해결 이슈 목록*"))));

        StringBuilder issueListBuilder = new StringBuilder();
        IntStream.range(0, openIssues.size()).forEach(index -> {
            NotSolvedIssueRow issue = openIssues.get(index);
            String threadLink = issue.getThreadLink(workspaceUrl);
            String formattedDate = issue.getFormattedDate();
            String assigneeDisplay = issue.getAssigneeDisplay();
            issueListBuilder.append(String.format("%d. <%s|%s> (%s) - %s\n",
                    index + 1, threadLink, issue.title(), formattedDate, assigneeDisplay));
        });

        blocks.add(section(s -> s.text(markdownText(issueListBuilder.toString().trim()))));

        return blocks;
    }

    private static LayoutBlock buildActionButtons(String buttonValue) {
        List<BlockElement> buttons = new ArrayList<>();

        // 채널에 공개 버튼
        buttons.add(ButtonElement.builder()
                .actionId(ACTION_PUBLISH_STATS)
                .text(plainText(pt -> pt.emoji(true).text(":mega: 채널에 공개")))
                .value(buttonValue)
                .build());

        // 담당자 태그 버튼
        buttons.add(ButtonElement.builder()
                .actionId(ACTION_TAG_ASSIGNEES)
                .text(plainText(pt -> pt.emoji(true).text(":man-raising-hand: 담당자 태그")))
                .value(buttonValue)
                .build());

        return actions(a -> a.elements(buttons));
    }

    private static LayoutBlock buildTagAssigneeButton(String buttonValue) {
        ButtonElement tagButton = ButtonElement.builder()
                .actionId(ACTION_TAG_ASSIGNEES)
                .text(plainText(pt -> pt.emoji(true).text(":man-raising-hand: 담당자 태그")))
                .value(buttonValue)
                .build();

        return actions(a -> a.elements(List.of(tagButton)));
    }
}
