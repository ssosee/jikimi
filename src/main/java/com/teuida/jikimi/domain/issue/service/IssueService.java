package com.teuida.jikimi.domain.issue.service;

import com.teuida.jikimi.common.TimeProvider;
import com.teuida.jikimi.common.annotation.IssueLogging;
import com.teuida.jikimi.common.enums.ActionType;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
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
import com.teuida.jikimi.domain.issue.service.dto.CreateIssueRequest;
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

        // 이슈 저장
        IssueEntity issueEntity = IssueEntity.create(request);
        issueEntityRepository.save(issueEntity);

        // 이슈 애플리케이션 타입 저장
        Set<ApplicationType> applicationTypes = request.applicationTypes();
        Set<IssueApplicationEntity> applicationEntities = applicationTypes.stream()
                .map(type -> IssueApplicationEntity.create(issueEntity, type))
                .collect(Collectors.toSet());
        issueApplicationEntityRepository.saveAll(applicationEntities);

        // 이슈 코스 타입 저장
        Set<CourseType> courseTypes = request.courseTypes();
        Set<IssueCourseEntity> courseEntities = courseTypes.stream()
                .map(type -> IssueCourseEntity.create(issueEntity, type))
                .collect(Collectors.toSet());
        issueCourseEntityRepository.saveAll(courseEntities);

        // 이슈 팀 저장
        Set<IssueUsergroupEntity> usergroupEntities = request.usergroupId().stream()
                .map(usergroupId -> IssueUsergroupEntity.create(issueEntity, usergroupId))
                .collect(Collectors.toSet());
        issueUsergroupEntityRepository.saveAll(usergroupEntities);

        return Issue.of(issueEntity, applicationEntities, courseEntities, usergroupEntities);
    }

    @Transactional
    public void updateIssue(Issue issue, String slackMessageTs) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issue.id())
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 메시지 타임 스탬프 변경
        findIssueEntity.changeSlackMessageTs(slackMessageTs);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.ASSIGNED)
    public Issue assign(Long issueId, String slackAssigneeId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈 담당자 변경
        findIssueEntity.changeSlackAssigneeId(slackAssigneeId);

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

        return Issue.from(findIssueEntity);
    }

    @Transactional
    @IssueLogging(actionType = ActionType.DELETED)
    public Issue delete(Long issueId, String slackReporterId) {
        // 이슈 조회
        IssueEntity findIssueEntity = issueEntityRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(IssueEntity.class));

        // 이슈가 완료된 상태이면
        if (findIssueEntity.isClosed()) {
            throw new IllegalStateException("완료된 이슈는 삭제할 수 없습니다.");
        }

        // 이슈 제보자와 삭제 요청자가 다르면
        if (!findIssueEntity.isEqualsSlackReporterId(slackReporterId)) {
            throw new IllegalStateException("이슈 제보자만 이슈를 삭제할 수 있습니다.");
        }

        // 이슈 삭제
        findIssueEntity.delete(timeProvider.nowDateTime());
        issueCourseEntityRepository.bulkDelete(issueId, timeProvider.nowDateTime());
        issueUsergroupEntityRepository.bulkDelete(issueId, timeProvider.nowDateTime());
        issueApplicationEntityRepository.bulkDelete(issueId, timeProvider.nowDateTime());

        return Issue.from(findIssueEntity);
    }
}
