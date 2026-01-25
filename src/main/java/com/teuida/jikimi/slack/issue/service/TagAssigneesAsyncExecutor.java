package com.teuida.jikimi.slack.issue.service;

import com.slack.api.methods.MethodsClient;
import com.teuida.jikimi.domain.issue.service.IssueStatsService;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.OpenIssueWithUsergroupsRow;
import com.teuida.jikimi.slack.issue.parser.IssueStatsParameterParser;
import com.teuida.jikimi.slack.issue.service.IssueStatsCommandHandlerService.IssueStatsButtonValue;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class TagAssigneesAsyncExecutor {

    private final IssueStatsService issueStatsService;
    private final ObjectMapper objectMapper;

    @Async
    public void execute(MethodsClient client, String channelId, String userId, String buttonValue) {
        try {
            IssueStatsButtonValue parsedValue = parseButtonValue(buttonValue);
            IssueStatsFilter filter = IssueStatsParameterParser.parse(parsedValue.commandText());

            // 미해결 이슈 목록 (담당팀 정보 포함) 조회
            List<OpenIssueWithUsergroupsRow> openIssues =
                    issueStatsService.findOpenIssuesWithUsergroups(filter);

            // 각 이슈 스레드에 댓글 작성
            for (OpenIssueWithUsergroupsRow issue : openIssues) {
                String message = buildTagMessage(issue);

                client.chatPostMessage(builder -> builder
                        .channel(issue.slackChannelId())
                        .threadTs(issue.slackMessageTs())
                        .text(message)
                );
            }

            // 완료 메시지 전송 (ephemeral)
            client.chatPostEphemeral(builder -> builder
                    .channel(channelId)
                    .user(userId)
                    .text(String.format(":white_check_mark: %d개 미해결 이슈의 담당자에게 태그 메시지를 전송했습니다.", openIssues.size()))
            );
        } catch (Exception e) {
            log.error("담당자 태그 처리 중 오류 발생", e);
        }
    }

    private String buildTagMessage(OpenIssueWithUsergroupsRow issue) {
        if (issue.hasAssignee()) {
            return String.format("<@%s> 이슈 해결 요청 드립니다.", issue.slackAssigneeId());
        }

        if (issue.hasUsergroups()) {
            return String.format("<!subteam^%s> 이슈 할당 요청 드립니다.", issue.getFirstUsergroupId());
        }

        return "담당자 또는 담당팀이 지정되지 않은 이슈입니다. 할당이 필요합니다.";
    }

    private IssueStatsButtonValue parseButtonValue(String buttonValue) {
        try {
            return objectMapper.readValue(buttonValue, IssueStatsButtonValue.class);
        } catch (Exception e) {
            throw new RuntimeException("버튼 값 파싱 실패", e);
        }
    }
}
