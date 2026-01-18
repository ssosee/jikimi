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
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.repository.IssueApplicationEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueCourseEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueQueryRepository;
import com.teuida.jikimi.domain.issue.repository.IssueUsergroupEntityRepository;
import com.teuida.jikimi.domain.issue.service.dto.AssignIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.CreateJiraIssueRequest;
import com.teuida.jikimi.domain.issue.service.dto.SolveIssueRequest;
import com.teuida.jikimi.jira.service.dto.JiraIssue;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueService {
    private final TimeProvider timeProvider;
    private final IssueQueryRepository issueQueryRepository;
    private final IssueEntityRepository issueEntityRepository;
    private final IssueApplicationEntityRepository issueApplicationEntityRepository;
    private final IssueCourseEntityRepository issueCourseEntityRepository;
    private final IssueUsergroupEntityRepository issueUsergroupEntityRepository;

    @Transactional
    @IssueLogging(actionType = ActionType.CREATED)
    public Issue createIssue(CreateIssueRequest request) {
        LocalDateTime now = timeProvider.nowDateTime();

        // 이슈 저장
        IssueEntity issueEntity = IssueEntity.create(request);
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

        return Issue.of(issueEntity, applicationEntities, courseEntities, usergroupEntities);
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

        // 이슈 애플리케이션 조회
        Set<IssueApplicationEntity> findIssueApplicationEntities = issueApplicationEntityRepository.findByIssueEntity(
                findIssueEntity);

        // 이슈 코스 조회
        Set<IssueCourseEntity> findIssueCourseEntities = issueCourseEntityRepository.findByIssueEntity(findIssueEntity);

        // 이슈 유저 그룹 조회
        Set<IssueUsergroupEntity> findIssueUsergroupEntites = issueUsergroupEntityRepository.findByIssueEntity(findIssueEntity);

        return Issue.of(findIssueEntity, findIssueApplicationEntities, findIssueCourseEntities, findIssueUsergroupEntites);
    }

    public Issue getIssue(Long issueId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 애플리케이션 조회
        Set<IssueApplicationEntity> findIssueApplicationEntities = issueApplicationEntityRepository.findByIssueEntity(
                findIssueEntity);

        // 이슈 코스 조회
        Set<IssueCourseEntity> findIssueCourseEntities = issueCourseEntityRepository.findByIssueEntity(findIssueEntity);

        // 이슈 유저 그룹 조회
        Set<IssueUsergroupEntity> findIssueUsergroupEntites = issueUsergroupEntityRepository.findByIssueEntity(findIssueEntity);

        return Issue.of(findIssueEntity, findIssueApplicationEntities, findIssueCourseEntities, findIssueUsergroupEntites);
    }

    public Issue getOnlyIssue(Long issueId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        return Issue.from(findIssueEntity);
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

        // 이슈 애플리케이션 조회
        Set<IssueApplicationEntity> findIssueApplicationEntities = issueApplicationEntityRepository.findByIssueEntity(
                findIssueEntity);

        // 이슈 코스 조회
        Set<IssueCourseEntity> findIssueCourseEntities = issueCourseEntityRepository.findByIssueEntity(findIssueEntity);

        // 이슈 유저 그룹 조회
        Set<IssueUsergroupEntity> findIssueUsergroupEntites = issueUsergroupEntityRepository.findByIssueEntity(findIssueEntity);

        return Issue.of(findIssueEntity, findIssueApplicationEntities, findIssueCourseEntities, findIssueUsergroupEntites);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.CREATED_JIRA)
    public Issue applyJiraIssue(CreateJiraIssueRequest request, JiraIssue jiraIssue) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(request.getIssueId())
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 Jira 이슈 정보 변경
        findIssueEntity.changeJiraIssue(jiraIssue);

        return Issue.from(findIssueEntity);
    }
}
