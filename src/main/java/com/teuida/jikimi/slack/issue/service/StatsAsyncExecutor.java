package com.teuida.jikimi.slack.issue.service;

import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.teuida.jikimi.domain.issue.service.IssueStatsService;
import com.teuida.jikimi.slack.issue.IssueStatsBlockBuilder;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.dto.NotSolvedIssueRow;
import com.teuida.jikimi.slack.issue.service.IssueStatsCommandHandlerService.IssueStatsButtonValue;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class StatsAsyncExecutor {

    private final IssueStatsService issueStatsService;
    private final ObjectMapper objectMapper;

    @Async
    public void execute(MethodsClient client, String channelId, String userId, IssueStatsFilter filter, String commandText,
                        String workspaceUrl) {
        // 2. 통계 조회
        IssueStatsRow result = issueStatsService.getIssueStats(filter);

        // 3. 버튼 value 생성 (이슈 ID 목록 직렬화)
        String buttonValue = serializeButtonValue(result.getNotSolvedIssueRows(), commandText);

        // 4. 블록 생성
        List<LayoutBlock> blocks = result.isEmpty()
                ? IssueStatsBlockBuilder.buildEmptyBlocks(commandText)
                : IssueStatsBlockBuilder.buildStatsBlocks(result, workspaceUrl, buttonValue);

        // 5. 응답 전송 (ephemeral - 요청자만 볼 수 있음)
        try {
            client.chatPostEphemeral(r -> r
                    .channel(channelId)
                    .user(userId)
                    .blocks(blocks)
                    .text("이슈 통계")
            );
        } catch (IOException | SlackApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeButtonValue(List<NotSolvedIssueRow> openIssues, String commandText) {
        List<Long> issueIds = openIssues.stream()
                .map(NotSolvedIssueRow::issueId)
                .collect(Collectors.toList());

        IssueStatsButtonValue buttonValue = new IssueStatsButtonValue(issueIds, commandText);

        try {
            return objectMapper.writeValueAsString(buttonValue);
        } catch (Exception e) {
            throw new RuntimeException("버튼 값 직렬화 실패", e);
        }
    }
}
