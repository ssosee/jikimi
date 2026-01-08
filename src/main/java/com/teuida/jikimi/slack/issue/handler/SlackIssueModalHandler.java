package com.teuida.jikimi.slack.issue.handler;

import static com.slack.api.model.block.Blocks.actions;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.asOptions;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.asElements;
import static com.slack.api.model.block.element.BlockElements.button;
import static com.slack.api.model.block.element.BlockElements.overflowMenu;
import static com.slack.api.model.block.element.BlockElements.usersSelect;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_DELETE_ISSUE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SELECT_ASSIGNEE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_SOLVE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_ASSIGN_TO_ME;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_CREATE_TICKET;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_DELETE_ISSUE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.VALUE_SOLVE;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.ViewSubmissionContext;
import com.slack.api.bolt.request.builtin.ViewSubmissionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.model.block.composition.TextObject;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.service.IssueService;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.slack.service.SlackService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SlackIssueModalHandler extends SlackIssueCommandHandler {

    private final IssueService issueService;

    public SlackIssueModalHandler(SlackService slackService, IssueService issueService) {
        this.issueService = issueService;
        super(slackService);
    }

    @Override
    public void register(App app) {
        String callbackId = super.getCallbackId();
        app.viewSubmission(callbackId, this::handle);
    }

    public Response handle(ViewSubmissionRequest req, ViewSubmissionContext ctx) throws SlackApiException, IOException {
        // request 생성
        CreateIssueRequest createIssueRequest = CreateIssueRequest.of(req, ctx);

        // 이슈 생성
        Issue issue = issueService.createIssue(createIssueRequest);

        // 채널에 메시지 전송
        ChatPostMessageResponse response = ctx.client().chatPostMessage(builder -> builder
                .channel(issue.slackChannelId())
                .blocks(buildIssueBlocks(issue))
                .text(String.format("%s 이슈가 등록되었습니다.", issue.title()))
                .unfurlLinks(false)
                .unfurlMedia(false)
        );

        if (response.isOk()) {
            String messageTs = response.getMessage().getTs();
            issueService.updateIssue(issue, messageTs);
        }

        return ctx.ack();
    }

    private List<LayoutBlock> buildIssueBlocks(Issue issue) {
        List<LayoutBlock> blocks = new ArrayList<>();

        // 1. [헤더] 제목
        blocks.add(header(h -> h.text(plainText(pt -> pt
                .emoji(true)
                .text(String.format(":alert: %s  :rainbow-right:", issue.title()))
        ))));

        // 2. [구분선]
        blocks.add(divider());

        // 3. [메타 데이터] 2단 그리드 (Fields)
        // Row 1: 환경 & 애플리케이션
        List<TextObject> row1 = new ArrayList<>();
        row1.add(markdownText("*환경:* " + issue.environment()));
        row1.add(markdownText("*애플리케이션:* " + issue.applicationTypes().stream()
                .map(ApplicationType::getDisplayName)
                .collect(Collectors.joining(", "))));
        blocks.add(section(s -> s.fields(row1)));

        // Row 2: 코스 & 제보자
        List<TextObject> row2 = new ArrayList<>();
        String courseTypes = issue.courseTypes().stream()
                .map(CourseType::getDisplayName)
                .collect(Collectors.joining(", "));
        row2.add(markdownText("*코스:* " + courseTypes));
        row2.add(markdownText("*제보자:* " + "<@" + issue.slackReporterId() + ">"));
        blocks.add(section(s -> s.fields(row2)));

        // Row 3: 담당자 & 담당팀
        List<TextObject> row3 = new ArrayList<>();
        String usergroups = issue.slackAssignedUsergroupIds().stream()
                .map(usergroupId -> "<!subteam^" + usergroupId + ">")
                .collect(Collectors.joining(", "));
        row3.add(markdownText("*담당팀:* " + usergroups));
        row3.add(markdownText("*담당자:* " + (issue.slackAssigneeId() != null ? "<@" + issue.slackAssigneeId() + ">" : "-")));
        blocks.add(section(s -> s.fields(row3)));

        // Row 4: 이메일
        List<TextObject> row4 = new ArrayList<>();
        row4.add(markdownText("*이메일:* " + (issue.userEmail() != null ? issue.userEmail() : "-")));
        blocks.add(section(s -> s.fields(row4)));

        // 4. [구분선]
        blocks.add(divider());

        // 5. [본문] 이슈 내용
        blocks.add(header(h -> h.text(plainText(pt -> pt
                .emoji(true)
                .text(":blob-help: 이슈 내용")
        ))));
        String descriptionText = String.format("%s", issue.description());
        blocks.add(section(s -> s.text(markdownText(descriptionText))));

        // 6. [구분선]
        blocks.add(divider());

        blocks.add(section(s -> s.text(markdownText("📸 필요한 사진 또는 영상은 스레드 댓글에 업로드 해주세요."))));

        // 이슈가 해결된 경우
        if (issue.status() == IssueStatus.CLOSED) {
            return blocks;
        }

        // 담당자가 있는 경우
        if (StringUtils.hasText(issue.slackAssigneeId())) {
            // 7. [버튼 액션]
            blocks.add(header(h -> h.text(plainText(pt -> pt
                    .text("👇 이슈에 대해 다음 작업을 선택하세요."))
            )));
            blocks.add(actions(a -> a.blockId(String.valueOf(issue.id())).elements(asElements(
                    button(b -> b.text(plainText(":nyancat_big: 티켓 생성"))
                            .value(VALUE_CREATE_TICKET)
                            .actionId(ACTION_CREATE_TICKET)),
                    button(b -> b.text(plainText(":success: 해결 완료"))
                            .value(VALUE_SOLVE)
                            .actionId(ACTION_SOLVE))
            ))));

            return blocks;
        }

        // 담당자가 없는 경우
        else if (!StringUtils.hasText(issue.slackAssigneeId())) {
            // 7. [버튼 액션]
            blocks.add(header(h -> h.text(plainText(pt -> pt
                    .text("👇 아래 버튼을 눌러 담당자를 지정하세요."))
            )));
            blocks.add(actions(a -> a.blockId(String.valueOf(issue.id())).elements(asElements(
                    button(b -> b.text(plainText("나에게 할당"))
                            .value(VALUE_ASSIGN_TO_ME)
                            .actionId(ACTION_ASSIGN_TO_ME)),
                    usersSelect(u -> u.placeholder(plainText("할당할 팀원 ..."))
                            .actionId(ACTION_SELECT_ASSIGNEE)
                    ),
                    overflowMenu(o -> o
                            .actionId(ACTION_DELETE_ISSUE)
                            .options(asOptions(
                                    BlockCompositions.option(opt -> opt.text(plainText("🗑️ 삭제하기")).value(VALUE_DELETE_ISSUE)))
                            )
                    )
            ))));

            return blocks;
        }

        return blocks;
    }
}
