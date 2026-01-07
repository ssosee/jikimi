package com.teuida.jikimi.domain.issue.service;

import com.teuida.jikimi.common.annotation.IssueLogging;
import com.teuida.jikimi.common.enums.ActionType;
import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.domain.issue.entity.ApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.CourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.repository.ApplicationEntityRepository;
import com.teuida.jikimi.domain.issue.repository.CourseEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueEntityRepository;
import com.teuida.jikimi.domain.issue.repository.IssueQueryRepository;
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

    private final IssueQueryRepository issueQueryRepository;
    private final IssueEntityRepository issueEntityRepository;
    private final ApplicationEntityRepository applicationEntityRepository;
    private final CourseEntityRepository courseEntityRepository;

    @Transactional
    @IssueLogging(actionType = ActionType.CREATED)
    public Issue createIssue(CreateIssueRequest request) {

        // 이슈 저장
        IssueEntity issueEntity = IssueEntity.create(request);
        issueEntityRepository.save(issueEntity);

        // 이슈 애플리케이션 타입 저장
        Set<ApplicationType> applicationTypes = request.applicationTypes();
        Set<ApplicationEntity> applicationEntities = applicationTypes.stream()
                .map(type -> ApplicationEntity.create(type, issueEntity))
                .collect(Collectors.toSet());
        applicationEntityRepository.saveAll(applicationEntities);

        // 이슈 코스 타입 저장
        Set<CourseType> courseTypes = request.courseTypes();
        Set<CourseEntity> courseEntities = courseTypes.stream()
                .map(type -> CourseEntity.create(type, issueEntity))
                .collect(Collectors.toSet());
        courseEntityRepository.saveAll(courseEntities);

        return Issue.create(issueEntity, applicationEntities, courseEntities);
    }
}
