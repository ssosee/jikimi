package com.teuida.jikimi.slack.issue.handler;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.input;
import static com.slack.api.model.block.composition.BlockCompositions.asOptions;
import static com.slack.api.model.block.composition.BlockCompositions.option;
import static com.slack.api.model.block.composition.BlockCompositions.optionGroup;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.block.element.BlockElements.emailTextInput;
import static com.slack.api.model.block.element.BlockElements.multiStaticSelect;
import static com.slack.api.model.block.element.BlockElements.plainTextInput;
import static com.slack.api.model.block.element.BlockElements.staticSelect;
import static com.slack.api.model.view.Views.view;
import static com.slack.api.model.view.Views.viewClose;
import static com.slack.api.model.view.Views.viewSubmit;
import static com.slack.api.model.view.Views.viewTitle;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_APPLICATION_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_COURSE_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_DESCRIPTION;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_ENVIRONMENT;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USER_EMAIL;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_APPLICATION_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_COURSE_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_DESCRIPTION;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_ENVIRONMENT;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USER_EMAIL;
import static com.teuida.jikimi.slack.service.SlackModalBuilder.PLAIN_TEXT;
import static com.teuida.jikimi.slack.service.SlackModalBuilder.createOptions;

import com.slack.api.bolt.App;
import com.slack.api.bolt.context.builtin.SlashCommandContext;
import com.slack.api.bolt.request.builtin.SlashCommandRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.Usergroup;
import com.slack.api.model.block.composition.OptionGroupObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.View;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.slack.SlackHandlerRegistrar;
import com.teuida.jikimi.slack.service.SlackService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SlackIssueCommandHandler implements SlackHandlerRegistrar {

    protected final SlackService slackService;

    @Override
    public void register(App app) {
        app.command("/issue", this::issueCommandHandler);
    }

    @Override
    public String getCallbackId() {
        return "issue_modal";
    }

    public Response issueCommandHandler(SlashCommandRequest req, SlashCommandContext ctx) throws SlackApiException, IOException {

        MethodsClient client = ctx.client();
        String botToken = ctx.getBotToken();
        String channelId = req.getPayload().getChannelId();

        // 슬랙 팀 정보 조회
        List<Usergroup> findUsergroups = slackService.fetchUserGroups(client, botToken);
        View view = buildIssueModal(channelId, ctx.getBotId(), findUsergroups, getCallbackId());

        client.viewsOpen(builder -> builder
                .triggerId(ctx.getTriggerId())
                .view(view)
        );

        return ctx.ack();
    }

    private View buildIssueModal(String channelId, String botId, List<Usergroup> usergroups, String callbackId) {
        // 사용자 그룹, 사용자 옵션 생성
        List<OptionObject> userGroupOptions = usergroups.stream()
                .map(ug -> option(plainText(ug.getName()), ug.getId()))
                .toList();

        // 옵션 그룹으로 묶기
        OptionGroupObject optionUsergroups = optionGroup(g -> g.label(plainText("팀(Teams)")).options(userGroupOptions));

        return view(view -> view
                .type("modal")
                .callbackId(callbackId)
                .privateMetadata(channelId)
                .botId(botId)
                .title(viewTitle(t -> t.type(PLAIN_TEXT).text("이슈 제보")))
                .submit(viewSubmit(s -> s.type(PLAIN_TEXT).text("제출")))
                .close(viewClose(c -> c.type(PLAIN_TEXT).text("닫기")))
                .blocks(asBlocks(
                        // 1. 실행 환경
                        input(i -> i.blockId(BLOCK_ENVIRONMENT).label(plainText("실행 환경")).element(
                                staticSelect(m -> m.actionId(ACTION_ENVIRONMENT)
                                        .placeholder(plainText(Environment.PROD.toString()))
                                        .initialOption(
                                                option(plainText(Environment.PROD.toString()), Environment.PROD.name()))
                                        .options(createOptions(Environment.class))
                                )
                        ).hint(plainText("실행 환경을 선택해주세요."))),

                        // 2. 코스
                        input(i -> i.blockId(BLOCK_COURSE_TYPE).optional(true).label(plainText("코스")).element(
                                multiStaticSelect(s -> s.actionId(ACTION_COURSE_TYPE)
                                        .placeholder(plainText(CourseType.ALL.toString()))
                                        .initialOptions(asOptions(
                                                option(plainText(CourseType.ALL.toString()), CourseType.ALL.name())))
                                        .options(createOptions(CourseType.class))
                                )
                        ).hint(plainText("코스를 선택해주세요."))),

                        // 3. 애플리케이션 종류
                        input(i -> i.blockId(BLOCK_APPLICATION_TYPE).label(plainText("애플리케이션 종류")).element(
                                multiStaticSelect(m -> m.actionId(ACTION_APPLICATION_TYPE)
                                        .placeholder(plainText("Server, Web..."))
                                        .maxSelectedItems(4)
                                        .options(createOptions(ApplicationType.class))
                                )
                        ).hint(plainText("애플리케이션 종류를 선택해주세요."))),

                        // 4. 이슈 제목
                        input(i -> i.blockId(BLOCK_TITLE).label(plainText("이슈 제목")).element(
                                plainTextInput(p -> p.actionId(ACTION_TITLE)
                                        .maxLength(100)
                                        .placeholder(plainText("마이크 이슈")))
                        )),

                        // 5. 이슈 내용 설명
                        input(i -> i.blockId(BLOCK_DESCRIPTION).label(plainText("이슈 내용 설명")).element(
                                plainTextInput(p -> p.actionId(ACTION_DESCRIPTION)
                                        .focusOnLoad(true)
                                        .multiline(true)
                                        .maxLength(1000)
                                        .placeholder(plainText("마이크가 활성화 되지 않습니다.")))
                        )),

                        // 7. 팀 or 개인 담당자 선택 -> BLOCK_ASSIGNEE_TEAM_OR_USER 사용
                        input(i -> i.blockId(BLOCK_USERGROUP).label(plainText("팀")).element(
                                multiStaticSelect(m -> m.actionId(ACTION_USERGROUP)
                                        .placeholder(plainText("개발팀"))
                                        .optionGroups(List.of(optionUsergroups))
                                )
                        ).hint(plainText("담당팀을 선택해주세요."))),

                        // 8. 사용자 이메일
                        input(i -> i.blockId(BLOCK_USER_EMAIL).optional(true).label(plainText("사용자 이메일")).element(
                                emailTextInput(p -> p.actionId(ACTION_USER_EMAIL)
                                        .placeholder(plainText("ralph@teuida.net")))
                        ).hint(plainText("사용자 이메일을 알려주세요.")))
                )));
    }
}
