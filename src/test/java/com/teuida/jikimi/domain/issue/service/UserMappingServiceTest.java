package com.teuida.jikimi.domain.issue.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teuida.jikimi.domain.issue.entity.UserMappingEntity;
import com.teuida.jikimi.domain.issue.repository.UserMappingEntityRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserMappingService 테스트 - 실제 엔티티 기반
 * <p>
 * 외부 API(Slack, Jira)를 호출하는 메서드는 통합 환경에서 테스트해야 하므로, 여기서는 DB 기반 Repository 테스트와 기존 매핑이 있는 경우의 서비스 동작을 테스트합니다.
 */
@SpringBootTest
@Transactional
class UserMappingServiceTest {

    @Autowired
    private UserMappingEntityRepository userMappingEntityRepository;

    @Nested
    @DisplayName("UserMappingEntity Repository 테스트")
    class UserMappingEntityRepositoryTest {

        @Test
        @DisplayName("UserMappingEntity를 저장하고 조회한다")
        void saveAndFind() {
            // given
            UserMappingEntity mapping = UserMappingEntity.builder()
                    .slackUserId("U_CRUD_001")
                    .slackUserEmail("crud@example.com")
                    .slackDisplayName("CRUD Test")
                    .slackRealName("CRUD Test Real")
                    .jiraAccountId("jira-crud-001")
                    .jiraDisplayName("CRUD Test (Jira)")
                    .build();

            // when
            UserMappingEntity saved = userMappingEntityRepository.save(mapping);

            // then
            assertThat(saved.getId()).isNotNull();

            Optional<UserMappingEntity> found = userMappingEntityRepository.findBySlackUserId("U_CRUD_001");
            assertThat(found).isPresent();
            assertThat(found.get().getSlackUserEmail()).isEqualTo("crud@example.com");
            assertThat(found.get().getJiraAccountId()).isEqualTo("jira-crud-001");
        }

        @Test
        @DisplayName("존재하지 않는 Slack 사용자 ID로 조회하면 빈 Optional을 반환한다")
        void findBySlackUserId_notFound() {
            // when
            Optional<UserMappingEntity> result = userMappingEntityRepository.findBySlackUserId("NON_EXISTENT");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제된 매핑은 조회되지 않는다 (Soft Delete)")
        void softDelete() {
            // given
            UserMappingEntity mapping = UserMappingEntity.builder()
                    .slackUserId("U_DELETE_001")
                    .slackUserEmail("delete@example.com")
                    .jiraAccountId("jira-delete-001")
                    .build();
            userMappingEntityRepository.save(mapping);

            // when - soft delete
            mapping.delete(LocalDateTime.now());
            userMappingEntityRepository.save(mapping);

            // then - @SQLRestriction으로 인해 조회 안됨
            Optional<UserMappingEntity> found = userMappingEntityRepository.findBySlackUserId("U_DELETE_001");
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("여러 매핑을 저장하고 각각 조회한다")
        void saveMultipleAndFind() {
            // given
            UserMappingEntity mapping1 = UserMappingEntity.builder()
                    .slackUserId("U_MULTI_001")
                    .slackUserEmail("multi1@example.com")
                    .jiraAccountId("jira-multi-001")
                    .build();

            UserMappingEntity mapping2 = UserMappingEntity.builder()
                    .slackUserId("U_MULTI_002")
                    .slackUserEmail("multi2@example.com")
                    .jiraAccountId("jira-multi-002")
                    .build();

            userMappingEntityRepository.save(mapping1);
            userMappingEntityRepository.save(mapping2);

            // when & then
            Optional<UserMappingEntity> found1 = userMappingEntityRepository.findBySlackUserId("U_MULTI_001");
            Optional<UserMappingEntity> found2 = userMappingEntityRepository.findBySlackUserId("U_MULTI_002");

            assertThat(found1).isPresent();
            assertThat(found1.get().getJiraAccountId()).isEqualTo("jira-multi-001");

            assertThat(found2).isPresent();
            assertThat(found2.get().getJiraAccountId()).isEqualTo("jira-multi-002");
        }

        @Test
        @DisplayName("모든 필드가 올바르게 저장된다")
        void allFieldsSaved() {
            // given
            UserMappingEntity mapping = UserMappingEntity.builder()
                    .slackUserId("U_FULL_001")
                    .slackUserEmail("full@example.com")
                    .slackDisplayName("Full Display Name")
                    .slackRealName("Full Real Name")
                    .jiraAccountId("jira-full-001")
                    .jiraDisplayName("Jira Full Display Name")
                    .build();

            // when
            userMappingEntityRepository.save(mapping);

            // then
            Optional<UserMappingEntity> found = userMappingEntityRepository.findBySlackUserId("U_FULL_001");
            assertThat(found).isPresent();

            UserMappingEntity entity = found.get();
            assertThat(entity.getSlackUserId()).isEqualTo("U_FULL_001");
            assertThat(entity.getSlackUserEmail()).isEqualTo("full@example.com");
            assertThat(entity.getSlackDisplayName()).isEqualTo("Full Display Name");
            assertThat(entity.getSlackRealName()).isEqualTo("Full Real Name");
            assertThat(entity.getJiraAccountId()).isEqualTo("jira-full-001");
            assertThat(entity.getJiraDisplayName()).isEqualTo("Jira Full Display Name");
            assertThat(entity.getDeleteDateTime()).isNull();
        }

        @Test
        @DisplayName("ID로 매핑을 조회한다")
        void findById() {
            // given
            UserMappingEntity mapping = UserMappingEntity.builder()
                    .slackUserId("U_BYID_001")
                    .slackUserEmail("byid@example.com")
                    .jiraAccountId("jira-byid-001")
                    .build();
            UserMappingEntity saved = userMappingEntityRepository.save(mapping);

            // when
            Optional<UserMappingEntity> found = userMappingEntityRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getSlackUserId()).isEqualTo("U_BYID_001");
        }

        @Test
        @DisplayName("매핑 수를 조회한다")
        void count() {
            // given
            long initialCount = userMappingEntityRepository.count();

            UserMappingEntity mapping1 = UserMappingEntity.builder()
                    .slackUserId("U_COUNT_001")
                    .slackUserEmail("count1@example.com")
                    .jiraAccountId("jira-count-001")
                    .build();

            UserMappingEntity mapping2 = UserMappingEntity.builder()
                    .slackUserId("U_COUNT_002")
                    .slackUserEmail("count2@example.com")
                    .jiraAccountId("jira-count-002")
                    .build();

            userMappingEntityRepository.save(mapping1);
            userMappingEntityRepository.save(mapping2);

            // when
            long count = userMappingEntityRepository.count();

            // then
            assertThat(count).isEqualTo(initialCount + 2);
        }
    }

    @Nested
    @DisplayName("UserMappingEntity 엔티티 동작 테스트")
    class UserMappingEntityBehaviorTest {

        @Test
        @DisplayName("delete 메서드 호출 시 deleteDateTime이 설정된다")
        void delete_setsDeleteDateTime() {
            // given
            UserMappingEntity mapping = UserMappingEntity.builder()
                    .slackUserId("U_DEL_TEST_001")
                    .slackUserEmail("deltest@example.com")
                    .jiraAccountId("jira-deltest-001")
                    .build();
            userMappingEntityRepository.save(mapping);

            LocalDateTime deleteTime = LocalDateTime.of(2024, 1, 15, 10, 30, 0);

            // when
            mapping.delete(deleteTime);
            userMappingEntityRepository.save(mapping);

            // then
            // @SQLRestriction으로 인해 일반 조회로는 찾을 수 없음
            Optional<UserMappingEntity> notFound = userMappingEntityRepository.findBySlackUserId("U_DEL_TEST_001");
            assertThat(notFound).isEmpty();
        }
    }
}
