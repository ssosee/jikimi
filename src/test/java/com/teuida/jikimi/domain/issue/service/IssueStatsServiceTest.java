package com.teuida.jikimi.domain.issue.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teuida.jikimi.common.TimeProvider;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import com.teuida.jikimi.domain.issue.entity.embedded.Embeddings;
import com.teuida.jikimi.domain.issue.repository.IssueEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueQueryRepository;
import com.teuida.jikimi.domain.issue.repository.IssueUsergroupEntityRepository;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter.Period;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.dto.OpenIssueWithUsergroupsRow;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class IssueStatsServiceTest {

    // Default embeddings for tests (3-dimensional for simplicity)
    private static final List<Double> DEFAULT_EMBEDDINGS = List.of(0.1, 0.2, 0.3);
    @Autowired
    private IssueEntityRepository issueEntityRepository;
    @Autowired
    private IssueUsergroupEntityRepository issueUsergroupEntityRepository;
    @Autowired
    private IssueQueryRepository issueQueryRepository;
    private IssueStatsService issueStatsService;

    private String nextUniqueId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
    }

    @BeforeEach
    void setUp() {
        // Initialize service with fixed clock
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                LocalDateTime.of(2024, 1, 15, 10, 0, 0).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        ));
        issueStatsService = new IssueStatsService(timeProvider, issueQueryRepository);
    }

    // Helper methods
    private IssueEntity createIssueWithStatus(IssueStatus status) {
        String uniqueId = nextUniqueId();
        IssueEntity.IssueEntityBuilder builder = IssueEntity.builder()
                .slackChannelId("C" + uniqueId)
                .slackReporterId("U" + uniqueId)
                .slackMessageTs("1234567890." + uniqueId)
                .environment(Environment.DEV)
                .status(status)
                .title("테스트 이슈 " + uniqueId)
                .description("테스트 설명")
                .embeddings(new Embeddings(DEFAULT_EMBEDDINGS));

        if (status == IssueStatus.IN_PROGRESS || status == IssueStatus.CLOSED) {
            builder.slackAssigneeId("A" + uniqueId);
        }

        return issueEntityRepository.saveAndFlush(builder.build());
    }

    private void createUsergroup(IssueEntity issueEntity, String usergroupId) {
        IssueUsergroupEntity usergroup = IssueUsergroupEntity.builder()
                .issueEntity(issueEntity)
                .slackUsergroupId(usergroupId)
                .createDateTime(LocalDateTime.now())
                .build();
        issueUsergroupEntityRepository.saveAndFlush(usergroup);
    }

    @Nested
    @DisplayName("getIssueStats")
    class GetIssueStats {

        @Test
        @DisplayName("기간 내 이슈 통계를 조회한다")
        void getIssueStats_success() {
            // given
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.IN_PROGRESS);
            createIssueWithStatus(IssueStatus.CLOSED);

            IssueStatsFilter filter = IssueStatsFilter.builder()
                    .period(Period.WEEK)
                    .build();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.getTotalCount()).isEqualTo(4);
            assertThat(stats.getCount(IssueStatus.OPEN)).isEqualTo(2);
            assertThat(stats.getCount(IssueStatus.IN_PROGRESS)).isEqualTo(1);
            assertThat(stats.getCount(IssueStatus.CLOSED)).isEqualTo(1);
        }

        @Test
        @DisplayName("이슈가 없으면 빈 통계를 반환한다")
        void getIssueStats_empty() {
            // given
            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.isEmpty()).isTrue();
            assertThat(stats.getTotalCount()).isZero();
        }

        @Test
        @DisplayName("특정 상태로 필터링하여 통계를 조회한다")
        void getIssueStats_withStatusFilter() {
            // given
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.CLOSED);

            IssueStatsFilter filter = IssueStatsFilter.builder()
                    .status(IssueStatus.OPEN)
                    .period(Period.WEEK)
                    .build();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.getTotalCount()).isEqualTo(2);
            assertThat(stats.getCount(IssueStatus.OPEN)).isEqualTo(2);
            assertThat(stats.getCount(IssueStatus.CLOSED)).isZero();
        }

        @Test
        @DisplayName("미해결 이슈 목록이 통계에 포함된다")
        void getIssueStats_includesNotSolvedIssues() {
            // given
            IssueEntity openIssue = createIssueWithStatus(IssueStatus.OPEN);
            IssueEntity inProgressIssue = createIssueWithStatus(IssueStatus.IN_PROGRESS);
            createIssueWithStatus(IssueStatus.CLOSED);

            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.getNotSolvedIssueRows()).hasSize(2);
            assertThat(stats.getNotSolvedIssueRows())
                    .extracting("issueId")
                    .containsExactlyInAnyOrder(openIssue.getId(), inProgressIssue.getId());
        }

        @Test
        @DisplayName("퍼센티지가 올바르게 계산된다")
        void getIssueStats_percentageCalculation() {
            // given
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.OPEN);
            createIssueWithStatus(IssueStatus.CLOSED);
            createIssueWithStatus(IssueStatus.CLOSED);

            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.getPercentage(IssueStatus.OPEN)).isEqualTo(50.0);
            assertThat(stats.getPercentage(IssueStatus.CLOSED)).isEqualTo(50.0);
            assertThat(stats.getPercentage(IssueStatus.IN_PROGRESS)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("월간 기간 필터로 통계를 조회한다")
        void getIssueStats_monthPeriod() {
            // given
            createIssueWithStatus(IssueStatus.OPEN);

            IssueStatsFilter filter = IssueStatsFilter.builder()
                    .period(Period.MONTH)
                    .build();

            // when
            IssueStatsRow stats = issueStatsService.getIssueStats(filter);

            // then
            assertThat(stats.getPeriodDisplayName()).isEqualTo("최근 30일");
            assertThat(stats.getTotalCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findOpenIssuesWithUsergroups")
    class FindOpenIssuesWithUsergroups {

        @Test
        @DisplayName("미해결 이슈와 담당 유저그룹을 함께 조회한다")
        void findOpenIssuesWithUsergroups_success() {
            // given
            IssueEntity openIssue = createIssueWithStatus(IssueStatus.OPEN);
            createUsergroup(openIssue, "S001");
            createUsergroup(openIssue, "S002");

            IssueEntity inProgressIssue = createIssueWithStatus(IssueStatus.IN_PROGRESS);
            createUsergroup(inProgressIssue, "S003");

            // CLOSED 이슈는 조회되지 않음
            IssueEntity closedIssue = createIssueWithStatus(IssueStatus.CLOSED);
            createUsergroup(closedIssue, "S004");

            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            List<OpenIssueWithUsergroupsRow> result = issueStatsService.findOpenIssuesWithUsergroups(filter);

            // then
            assertThat(result).hasSize(2);

            OpenIssueWithUsergroupsRow openIssueRow = result.stream()
                    .filter(row -> row.issueId().equals(openIssue.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(openIssueRow.slackUsergroupIds()).containsExactlyInAnyOrder("S001", "S002");

            OpenIssueWithUsergroupsRow inProgressRow = result.stream()
                    .filter(row -> row.issueId().equals(inProgressIssue.getId()))
                    .findFirst()
                    .orElseThrow();
            assertThat(inProgressRow.slackUsergroupIds()).containsExactly("S003");
        }

        @Test
        @DisplayName("유저그룹이 없는 이슈도 조회된다")
        void findOpenIssuesWithUsergroups_noUsergroups() {
            // given
            createIssueWithStatus(IssueStatus.OPEN);

            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            List<OpenIssueWithUsergroupsRow> result = issueStatsService.findOpenIssuesWithUsergroups(filter);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).slackUsergroupIds()).isEmpty();
        }

        @Test
        @DisplayName("미해결 이슈가 없으면 빈 리스트를 반환한다")
        void findOpenIssuesWithUsergroups_empty() {
            // given
            createIssueWithStatus(IssueStatus.CLOSED);

            IssueStatsFilter filter = IssueStatsFilter.defaultIssueStateFilter();

            // when
            List<OpenIssueWithUsergroupsRow> result = issueStatsService.findOpenIssuesWithUsergroups(filter);

            // then
            assertThat(result).isEmpty();
        }
    }
}
