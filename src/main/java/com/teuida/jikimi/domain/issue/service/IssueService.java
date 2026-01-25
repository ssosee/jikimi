package com.teuida.jikimi.domain.issue.service;

import com.teuida.jikimi.common.TimeProvider;
import com.teuida.jikimi.common.annotation.IssueLogging;
import com.teuida.jikimi.common.enums.ActionType;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.domain.exception.IssueAccessDeniedException;
import com.teuida.jikimi.domain.exception.NotFoundException;
import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import com.teuida.jikimi.domain.issue.entity.embedded.Embeddings;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.model.IssueRelations;
import com.teuida.jikimi.domain.issue.repository.IssueApplicationEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueCourseEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueUsergroupEntityRepository;
import com.teuida.jikimi.domain.issue.service.dto.AssignIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateJiraIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.SolveIssueRequest;
import com.teuida.jikimi.jira.service.dto.JiraIssue;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService {
    public static final double SIMILARITY_SCORE_LIMIT = 0.75;

    private final TimeProvider timeProvider;
    private final IssueEntityRepository issueEntityRepository;
    private final IssueApplicationEntityRepository issueApplicationEntityRepository;
    private final IssueCourseEntityRepository issueCourseEntityRepository;
    private final IssueUsergroupEntityRepository issueUsergroupEntityRepository;

    @Transactional
    @IssueLogging(actionType = ActionType.CREATED)
    public Issue createIssue(CreateIssueRequest request, List<Double> embeddings) {
        LocalDateTime now = timeProvider.nowDateTime();

        // 이슈 저장
        IssueEntity issueEntity = IssueEntity.create(request, embeddings);
        issueEntityRepository.save(issueEntity);

        // 이슈 애플리케이션 타입 저장
        Set<ApplicationType> applicationTypes = request.getApplicationTypes();
        Set<IssueApplicationEntity> applicationEntities = applicationTypes.stream()
                .map(type -> IssueApplicationEntity.create(issueEntity, type, now))
                .collect(Collectors.toSet());
        issueApplicationEntityRepository.saveAll(applicationEntities);

        // 이슈 코스 타입 저장
        Set<CourseType> courseTypes = request.getCourseTypes();
        Set<IssueCourseEntity> courseEntities = courseTypes.stream()
                .map(type -> IssueCourseEntity.create(issueEntity, type, now))
                .collect(Collectors.toSet());
        issueCourseEntityRepository.saveAll(courseEntities);

        // 이슈 팀 저장
        Set<IssueUsergroupEntity> usergroupEntities = request.getUsergroupIds().stream()
                .map(usergroupId -> IssueUsergroupEntity.create(issueEntity, usergroupId, now))
                .collect(Collectors.toSet());
        issueUsergroupEntityRepository.saveAll(usergroupEntities);

        IssueRelations relations = new IssueRelations(applicationEntities, courseEntities, usergroupEntities);
        return Issue.of(issueEntity, relations);
    }

    @Transactional
    public void updateIssue(Issue issue, String slackMessageTs) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issue.getId())
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 메시지 타임 스탬프 변경
        findIssueEntity.changeSlackMessageTs(slackMessageTs);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.ASSIGNED)
    public Issue assign(AssignIssueRequest request) {
        Long issueId = request.getIssueId();
        String slackAssigneeId = request.getSlackAssigneeId();

        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 진행 상태로 변경
        findIssueEntity.inProgress(slackAssigneeId);

        var relations = loadRelations(findIssueEntity);
        return Issue.of(findIssueEntity, relations);
    }

    public Issue getIssue(Long issueId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        var relations = loadRelations(findIssueEntity);
        return Issue.of(findIssueEntity, relations);
    }

    public Issue getOnlyIssue(Long issueId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        return Issue.fromEntity(findIssueEntity);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.RESOLVE)
    public Issue solve(SolveIssueRequest request) {
        Long issueId = request.getIssueId();
        String requestUserId = request.getRequestUserId();

        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 담당자와 삭제 요청자가 다르면
        if (!findIssueEntity.isEqualsSlackAssigneeId(requestUserId)) {
            throw new IssueAccessDeniedException("이슈 담당자만 이슈를 완료 할 수 있습니다.");
        }

        // 이슈 상태 변경
        findIssueEntity.solve();

        var relations = loadRelations(findIssueEntity);
        return Issue.of(findIssueEntity, relations);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.CREATED_JIRA)
    public Issue applyJiraIssue(CreateJiraIssueRequest request, JiraIssue jiraIssue) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(request.getIssueId())
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 Jira 이슈 정보 변경
        findIssueEntity.changeJiraIssue(jiraIssue);

        return Issue.fromEntity(findIssueEntity);
    }

    public List<Issue> findTopSimilarityIssues(Long issueId, int limit) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        Embeddings targetEmbeddings = findIssueEntity.getEmbeddings();

        if (targetEmbeddings == null) {
            log.warn("해당 이슈는 임베딩 정보가 존재하지 않습니다.");
            return Collections.emptyList();
        }

        // 최근 이슈 2000개 조회
        List<IssueEntity> findIssueEntities = issueEntityRepository.findTop2000WithEmbeddingsExcluding(issueId);

        return findIssueEntities.stream()
                .map(issueEntity -> {
                    // 코사인 유사도 계산
                    double similarityScore = issueEntity.getEmbeddings().cosineSimilarity(targetEmbeddings);
                    return Issue.fromEntity(issueEntity, similarityScore);
                })
                .sorted(Comparator.comparingDouble(Issue::getSimilarityScore).reversed())
                .filter(issue -> issue.getSimilarityScore() > SIMILARITY_SCORE_LIMIT)
                .limit(limit)
                .toList();
    }

    /**
     * IssueEntity에 대한 관계 엔티티들을 조회하여 IssueRelations로 반환
     */
    private IssueRelations loadRelations(IssueEntity issueEntity) {
        Set<IssueApplicationEntity> applications = issueApplicationEntityRepository.findByIssueEntity(issueEntity);
        Set<IssueCourseEntity> courses = issueCourseEntityRepository.findByIssueEntity(issueEntity);
        Set<IssueUsergroupEntity> usergroups = issueUsergroupEntityRepository.findByIssueEntity(issueEntity);

        return new IssueRelations(applications, courses, usergroups);
    }
}
