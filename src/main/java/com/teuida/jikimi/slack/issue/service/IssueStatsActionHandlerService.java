package com.teuida.jikimi.slack.issue.service;

import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.block.LayoutBlock;
import com.teuida.jikimi.domain.issue.service.IssueStatsService;
import com.teuida.jikimi.slack.issue.IssueStatsBlockBuilder;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.parser.IssueStatsParameterParser;
import com.teuida.jikimi.slack.issue.service.IssueStatsCommandHandlerService.IssueStatsButtonValue;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueStatsActionHandlerService {

    private final IssueStatsService issueStatsService;
    private final TagAssigneesAsyncExecutor tagAssigneesAsyncExecutor;
    private final ObjectMapper objectMapper;

    @Value("${slack.workspace-url}")
    private String workspaceUrl;

    public Response handleTagAssignees(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        MethodsClient client = ctx.client();
        String channelId = req.getPayload().getChannel().getId();
        String userId = ctx.getRequestUserId();
        String buttonValue = req.getPayload().getActions().getFirst().getValue();

        // 즉시 응답 (3초 내) - ephemeral 메시지
        client.chatPostEphemeral(builder -> builder
                .channel(channelId)
                .user(userId)
                .text(":hourglass_flowing_sand: 담당자 태그 처리 중...")
        );

        // 비동기 처리 실행 (별도 클래스를 통해 프록시 호출)
        tagAssigneesAsyncExecutor.execute(client, channelId, userId, buttonValue);

        return ctx.ack();
    }

    public Response handlePublishStats(BlockActionRequest req, ActionContext ctx)
            throws SlackApiException, IOException {
        MethodsClient client = ctx.client();
        String channelId = req.getPayload().getChannel().getId();

        // 버튼 값에서 커맨드 텍스트 추출
        String buttonValue = req.getPayload().getActions().getFirst().getValue();
        IssueStatsButtonValue parsedValue = parseButtonValue(buttonValue);

        // 이슈 필터 파싱 및 통계 조회
        IssueStatsFilter filter = IssueStatsParameterParser.parse(parsedValue.commandText());
        IssueStatsRow result = issueStatsService.getIssueStats(filter);

        // 블록 생성 (담당자 태그 버튼만 포함)
        List<LayoutBlock> blocks = IssueStatsBlockBuilder.buildStatsBlocksWithTagButtonOnly(
                result, workspaceUrl, buttonValue);

        // 채널에 공개 메시지 전송
        client.chatPostMessage(builder -> builder
                .channel(channelId)
                .blocks(blocks)
                .text("이슈 현황")
        );

        // 완료 메시지 전송 (ephemeral)
        client.chatPostEphemeral(builder -> builder
                .channel(channelId)
                .user(ctx.getRequestUserId())
                .text(":white_check_mark: 이슈 현황이 채널에 공개되었습니다.")
        );

        return ctx.ack();
    }

    private IssueStatsButtonValue parseButtonValue(String buttonValue) {
        try {
            return objectMapper.readValue(buttonValue, IssueStatsButtonValue.class);
        } catch (Exception e) {
            throw new RuntimeException("버튼 값 파싱 실패", e);
        }
    }
}
