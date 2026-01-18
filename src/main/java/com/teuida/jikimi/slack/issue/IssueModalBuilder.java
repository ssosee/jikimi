package com.teuida.jikimi.slack.issue;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.input;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.asOptions;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
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
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_JIRA_PRIORITY;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_JIRA_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.ACTION_USER_EMAIL;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_APPLICATION_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_COURSE_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_DESCRIPTION;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_ENVIRONMENT;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_JIRA_PRIORITY;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_JIRA_TYPE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_TITLE;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USERGROUP;
import static com.teuida.jikimi.slack.issue.IssueModalKeys.BLOCK_USER_EMAIL;

import com.slack.api.model.Usergroup;
import com.slack.api.model.block.composition.OptionGroupObject;
import com.slack.api.model.block.composition.OptionObject;
import com.slack.api.model.view.View;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraProjectResponse.IssueType;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse;
import com.teuida.jikimi.jira.client.dto.response.JiraSearchPriorityResponse.Value;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

abstract public class IssueModalBuilder {
    public static final String MODAL = "modal";
    public static final String HOME = "home";
    public static final String WORKFLOW_STEP = "workflow_step";
    public static final String PLAIN_TEXT = "plain_text";

    private static <E extends Enum<E>> List<OptionObject> createOptions(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(value -> option(plainText(value.toString()), value.name()))
                .collect(Collectors.toList());
    }

    public static View buildIssueModal(String channelId, List<Usergroup> usergroups, String callbackId) {
        // 사용자 그룹, 사용자 옵션 생성
        List<OptionObject> userGroupOptions = usergroups.stream()
                .map(ug -> option(plainText(ug.getName()), ug.getId()))
                .toList();

        // 옵션 그룹으로 묶기
        OptionGroupObject optionUsergroups = optionGroup(g -> g.label(plainText("팀(Teams)")).options(userGroupOptions));

        return view(view -> view
                .type(MODAL)
                .callbackId(callbackId)
                .privateMetadata(channelId)
                .title(viewTitle(t -> t.type(PLAIN_TEXT).text("이슈 제보")))
                .submit(viewSubmit(s -> s.type(PLAIN_TEXT).text("제출")))
                .close(viewClose(c -> c.type(PLAIN_TEXT).text("닫기")))
                .blocks(asBlocks(
                        // 1. 실행 환경
                        input(i -> i.blockId(BLOCK_ENVIRONMENT).label(plainText("실행 환경")).element(
                                staticSelect(m -> m
                                        .actionId(ACTION_ENVIRONMENT)
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
                        ).hint(plainText("코스를 선택해주세요(*ALL 을 선택하면 다른 코스를 선택할 수 없습니다.)"))),

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
                                        .initialOptions(asOptions(
                                                option(plainText("개발팀"), "S03A1B1R3JR")
                                        ))
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

    public static View buildJiraIssueModal(Issue issue, String callbackId, JiraProjectResponse jiraProject,
                                           JiraSearchPriorityResponse jiraSearchPriority) {

        List<IssueType> issueTypes = jiraProject.issueTypes();
        List<Value> values = jiraSearchPriority.values();

        return view(builder -> builder
                .type(MODAL)
                .callbackId(callbackId)
                .privateMetadata(issue.getId().toString())
                .title(viewTitle(t -> t.type(PLAIN_TEXT).text("Jira 티켓 생성")))
                .submit(viewSubmit(s -> s.type(PLAIN_TEXT).text("제출")))
                .close(viewClose(c -> c.type(PLAIN_TEXT).text("닫기")))
                .blocks(asBlocks(
                        // 이슈 제목 표시 (읽기 전용)
                        section(s -> s.text(markdownText("*이슈 제목*\n" + issue.getTitle()))),

                        divider(), // 구분선 추가 (선택사항)

                        // 우선순위 설정
                        input(i -> i.blockId(BLOCK_JIRA_PRIORITY)
                                .label(plainText("우선순위")).element(staticSelect(s -> s
                                        .actionId(ACTION_JIRA_PRIORITY)
                                        .placeholder(plainText("우선순위 선택"))
                                        .initialOption(
                                                option(plainText(values.getLast().name()), values.getLast().id()))
                                        .options(values.stream()
                                                .map(value -> option(plainText(value.name()), value.id()))
                                                .toList())
                                ))),

                        // 이슈타입 설정
                        input(i -> i.blockId(BLOCK_JIRA_TYPE)
                                .label(plainText("이슈타입")).element(staticSelect(s -> s
                                        .actionId(ACTION_JIRA_TYPE)
                                        .placeholder(plainText("이슈타입 선택"))
                                        .initialOption(
                                                option(plainText(issueTypes.getLast().name()), issueTypes.getLast().id()))
                                        .options(issueTypes.stream()
                                                .filter(issueType -> !issueType.subtask())
                                                .map(issueType -> option(plainText(issueType.name()), issueType.id()))
                                                .toList())
                                )))
                ))
        );
    }
}
