package com.teuida.jikimi.domain.issue.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.exception.IssueAccessDeniedException;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import com.teuida.jikimi.domain.issue.entity.embedded.Embeddings;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.repository.IssueApplicationEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueCourseEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueUsergroupEntityRepository;
import com.teuida.jikimi.domain.issue.service.dto.AssignIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateJiraIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.SolveIssueRequest;
import com.teuida.jikimi.jira.service.dto.JiraIssue;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
class IssueServiceTest {

    // Default embeddings for tests (3-dimensional for simplicity)
    private static final List<Double> DEFAULT_EMBEDDINGS = List.of(0.1, 0.2, 0.3);
    @Autowired
    private IssueService issueService;
    @Autowired
    private IssueEntityRepository issueEntityRepository;
    @Autowired
    private IssueApplicationEntityRepository issueApplicationEntityRepository;
    @Autowired
    private IssueCourseEntityRepository issueCourseEntityRepository;
    @Autowired
    private IssueUsergroupEntityRepository issueUsergroupEntityRepository;
    @Autowired
    private EntityManager entityManager;

    private String uniqueId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Helper methods
    private IssueEntity saveIssue() {
        String uid = uniqueId();
        IssueEntity issueEntity = IssueEntity.builder()
                .slackChannelId("C" + uid)
                .slackReporterId("U" + uid)
                .slackMessageTs("ts." + uid)
                .environment(Environment.DEV)
                .status(IssueStatus.OPEN)
                .title("테스트 이슈 " + uid)
                .description("테스트 설명")
                .userEmail("test" + uid + "@example.com")
                .embeddings(new Embeddings(DEFAULT_EMBEDDINGS))
                .build();
        return issueEntityRepository.save(issueEntity);
    }

    private IssueEntity saveIssueWithoutMessageTs() {
        String uid = uniqueId();
        IssueEntity issueEntity = IssueEntity.builder()
                .slackChannelId("C" + uid)
                .slackReporterId("U" + uid)
                // slackMessageTs is intentionally null for testing updateIssue
                .environment(Environment.DEV)
                .status(IssueStatus.OPEN)
                .title("테스트 이슈 " + uid)
                .description("테스트 설명")
                .userEmail("test" + uid + "@example.com")
                .embeddings(new Embeddings(DEFAULT_EMBEDDINGS))
                .build();
        return issueEntityRepository.save(issueEntity);
    }

    private IssueEntity saveIssueWithEmbeddings(List<Double> embeddingValues) {
        String uid = uniqueId();
        IssueEntity issueEntity = IssueEntity.builder()
                .slackChannelId("C" + uid)
                .slackReporterId("U" + uid)
                .slackMessageTs("ts." + uid)
                .environment(Environment.DEV)
                .status(IssueStatus.OPEN)
                .title("테스트 이슈 " + uid)
                .description("테스트 설명")
                .userEmail("test" + uid + "@example.com")
                .embeddings(new Embeddings(embeddingValues))
                .build();
        return issueEntityRepository.save(issueEntity);
    }

    private void saveRelations(IssueEntity issueEntity) {
        LocalDateTime now = LocalDateTime.now();

        IssueApplicationEntity appEntity = IssueApplicationEntity.builder()
                .issueEntity(issueEntity)
                .type(ApplicationType.IOS)
                .createDateTime(now)
                .build();
        issueApplicationEntityRepository.save(appEntity);

        IssueCourseEntity courseEntity = IssueCourseEntity.builder()
                .issueEntity(issueEntity)
                .type(CourseType.KOEN)
                .createDateTime(now)
                .build();
        issueCourseEntityRepository.save(courseEntity);

        IssueUsergroupEntity usergroupEntity = IssueUsergroupEntity.builder()
                .issueEntity(issueEntity)
                .slackUsergroupId("S001")
                .createDateTime(now)
                .build();
        issueUsergroupEntityRepository.save(usergroupEntity);
    }

    @Nested
    @DisplayName("createIssue")
    class CreateIssue {

        @Test
        @DisplayName("이슈를 생성하면 이슈와 관계 엔티티가 모두 저장된다")
        void createIssue_success() {
            // given
            String uid = uniqueId();
            CreateIssueRequest request = CreateIssueRequest.builder()
                    .requestUserId("U" + uid)
                    .slackChannelId("C" + uid)
                    .slackReporterId("U" + uid)
                    .applicationTypes(Set.of(ApplicationType.IOS, ApplicationType.ANDROID))
                    .courseTypes(Set.of(CourseType.KOEN))
                    .usergroupIds(Set.of("S001", "S002"))
                    .environment(Environment.PROD)
                    .title("테스트 이슈")
                    .description("테스트 설명입니다")
                    .userEmail("test@example.com")
                    .build();

            List<Double> embeddings = List.of(0.1, 0.2, 0.3);

            // when
            Issue issue = issueService.createIssue(request, embeddings);

            // then
            assertThat(issue.getId()).isNotNull();
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.OPEN);
            assertThat(issue.getTitle()).isEqualTo("테스트 이슈");
            assertThat(issue.getApplicationTypes()).containsExactlyInAnyOrder(ApplicationType.IOS, ApplicationType.ANDROID);
            assertThat(issue.getCourseTypes()).containsExactly(CourseType.KOEN);
        }
    }

    @Nested
    @DisplayName("updateIssue")
    class UpdateIssue {

        @Test
        @DisplayName("이슈의 Slack 메시지 타임스탬프를 업데이트한다")
        void updateIssue_success() {
            // given
            IssueEntity issueEntity = saveIssueWithoutMessageTs();
            Issue issue = Issue.fromEntity(issueEntity);
            String newMessageTs = "1234567890." + uniqueId();

            // when
            issueService.updateIssue(issue, newMessageTs);

            // then
            entityManager.flush();
            entityManager.clear();
            IssueEntity updated = issueEntityRepository.findById(issueEntity.getId()).orElseThrow();
            assertThat(updated.getSlackMessageTs()).isEqualTo(newMessageTs);
        }

        @Test
        @DisplayName("존재하지 않는 이슈를 업데이트하면 예외가 발생한다")
        void updateIssue_notFound() {
            // given
            Issue nonExistentIssue = Issue.builder().id(9999L).build();

            // when & then
            assertThatThrownBy(() -> issueService.updateIssue(nonExistentIssue, "1234567890.123456"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("assign")
    class Assign {

        @Test
        @DisplayName("이슈를 담당자에게 할당하면 상태가 IN_PROGRESS로 변경된다")
        void assign_success() {
            // given
            IssueEntity issueEntity = saveIssue();
            saveRelations(issueEntity);
            entityManager.flush();
            entityManager.clear();

            AssignIssueRequest request = new AssignIssueRequest("U123", issueEntity.getId(), "U456");

            // when
            Issue assignedIssue = issueService.assign(request);

            // then
            assertThat(assignedIssue.getStatus()).isEqualTo(IssueStatus.IN_PROGRESS);
            assertThat(assignedIssue.getSlackContext().getAssigneeId()).isEqualTo("U456");
        }

        @Test
        @DisplayName("존재하지 않는 이슈를 할당하면 예외가 발생한다")
        void assign_notFound() {
            // given
            AssignIssueRequest request = new AssignIssueRequest("U123", 9999L, "U456");

            // when & then
            assertThatThrownBy(() -> issueService.assign(request))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getIssue")
    class GetIssue {

        @Test
        @DisplayName("이슈 ID로 이슈를 조회한다")
        void getIssue_success() {
            // given
            IssueEntity issueEntity = saveIssue();
            saveRelations(issueEntity);
            entityManager.flush();
            entityManager.clear();

            // when
            Issue issue = issueService.getIssue(issueEntity.getId());

            // then
            assertThat(issue.getId()).isEqualTo(issueEntity.getId());
            assertThat(issue.getApplicationTypes()).hasSize(1);
        }

        @Test
        @DisplayName("존재하지 않는 이슈를 조회하면 예외가 발생한다")
        void getIssue_notFound() {
            assertThatThrownBy(() -> issueService.getIssue(9999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOnlyIssue")
    class GetOnlyIssue {

        @Test
        @DisplayName("관계 엔티티 없이 이슈만 조회한다")
        void getOnlyIssue_success() {
            // given
            IssueEntity issueEntity = saveIssue();
            entityManager.flush();
            entityManager.clear();

            // when
            Issue issue = issueService.getOnlyIssue(issueEntity.getId());

            // then
            assertThat(issue.getId()).isEqualTo(issueEntity.getId());
            assertThat(issue.getApplicationTypes()).isEmpty();
        }
    }

    @Nested
    @DisplayName("solve")
    class Solve {

        @Test
        @DisplayName("담당자가 이슈를 완료 처리하면 상태가 CLOSED로 변경된다")
        void solve_success() {
            // given
            IssueEntity issueEntity = saveIssue();
            issueEntity.inProgress("U456");
            issueEntityRepository.save(issueEntity);
            saveRelations(issueEntity);
            entityManager.flush();
            entityManager.clear();

            SolveIssueRequest request = new SolveIssueRequest("U456", issueEntity.getId());

            // when
            Issue solvedIssue = issueService.solve(request);

            // then
            assertThat(solvedIssue.getStatus()).isEqualTo(IssueStatus.CLOSED);
        }

        @Test
        @DisplayName("담당자가 아닌 사용자가 완료 처리하면 예외가 발생한다")
        void solve_accessDenied() {
            // given
            IssueEntity issueEntity = saveIssue();
            issueEntity.inProgress("U456");
            issueEntityRepository.save(issueEntity);
            entityManager.flush();
            entityManager.clear();

            SolveIssueRequest request = new SolveIssueRequest("OTHER_USER", issueEntity.getId());

            // when & then
            assertThatThrownBy(() -> issueService.solve(request))
                    .isInstanceOf(IssueAccessDeniedException.class)
                    .hasMessageContaining("담당자만");
        }

        @Test
        @DisplayName("존재하지 않는 이슈를 완료 처리하면 예외가 발생한다")
        void solve_notFound() {
            SolveIssueRequest request = new SolveIssueRequest("U456", 9999L);
            assertThatThrownBy(() -> issueService.solve(request))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("applyJiraIssue")
    class ApplyJiraIssue {

        @Test
        @DisplayName("이슈에 Jira 티켓 정보를 적용한다")
        void applyJiraIssue_success() {
            // given
            IssueEntity issueEntity = saveIssue();
            entityManager.flush();
            entityManager.clear();

            CreateJiraIssueRequest request = new CreateJiraIssueRequest(
                    "U123", issueEntity.getId(), "10001", "10002"
            );
            JiraIssue jiraIssue = JiraIssue.builder()
                    .key("XTDL-" + uniqueId())
                    .apiUrl("https://teuida.atlassian.net/rest/api/3/issue/17013")
                    .assigneeId("jira-user-123")
                    .build();

            // when
            Issue updatedIssue = issueService.applyJiraIssue(request, jiraIssue);

            // then
            assertThat(updatedIssue.getJiraContext().isCreated()).isTrue();
            assertThat(updatedIssue.getJiraContext().getAssigneeId()).isEqualTo("jira-user-123");
        }
    }

    @Nested
    @DisplayName("findTopSimilarityIssues")
    class FindTopSimilarityIssues {

        @Test
        @DisplayName("유사한 이슈들을 유사도 순으로 조회한다")
        void findTopSimilarityIssues_success() {
            // given
            IssueEntity targetIssue = saveIssueWithEmbeddings(List.of(1.0, 0.0, 0.0));
            IssueEntity similarIssue = saveIssueWithEmbeddings(List.of(0.9, 0.1, 0.0));
            IssueEntity differentIssue = saveIssueWithEmbeddings(List.of(0.0, 1.0, 0.0));
            entityManager.flush();
            entityManager.clear();

            // when
            List<Issue> similarIssues = issueService.findTopSimilarityIssues(targetIssue.getId(), 10);

            // then
            assertThat(similarIssues).hasSize(1);
            assertThat(similarIssues.get(0).getId()).isEqualTo(similarIssue.getId());
            assertThat(similarIssues.get(0).getSimilarityScore()).isGreaterThan(IssueService.SIMILARITY_SCORE_LIMIT);
        }

        @Test
        @DisplayName("임베딩이 없는 이슈는 빈 리스트를 반환한다")
        void findTopSimilarityIssues_noEmbeddings() {
            // given
            IssueEntity issueWithoutEmbeddings = saveIssue();
            entityManager.flush();
            entityManager.clear();

            // when
            List<Issue> result = issueService.findTopSimilarityIssues(issueWithoutEmbeddings.getId(), 10);

            // then
            assertThat(result).isEmpty();
        }
    }
}
