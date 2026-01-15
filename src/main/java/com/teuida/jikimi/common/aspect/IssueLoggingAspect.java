package com.teuida.jikimi.common.aspect;

import com.teuida.jikimi.common.annotation.IssueLogging;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueLogEntity;
import com.teuida.jikimi.domain.issue.model.Issue;
import com.teuida.jikimi.domain.issue.repository.IssueLogEntityRepository;
import com.teuida.jikimi.domain.issue.service.dto.IssueRequest;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IssueLoggingAspect {

    private final IssueLogEntityRepository issueLogEntityRepository;
    private final EntityManager entityManager;

    @AfterReturning(pointcut = "@annotation(issueLogging)", returning = "result")
    public void logIssueAction(JoinPoint joinPoint, Object result, IssueLogging issueLogging) {
        try {
            // 반환값에서 Issue 객체 추출
            if (!(result instanceof Issue issue)) {
                log.warn("Return type is not Issue. Skipping issue log creation.");
                return;
            }

            // Issue.id()로부터 IssueEntity 프록시 참조 획득 (추가 SELECT 쿼리 없이)
            Long issueId = issue.getId();
            IssueEntity issueEntity = entityManager.getReference(IssueEntity.class, issueId);

            // 메서드 파라미터에서 slackActorId 추출
            String slackActorId = extractSlackActorId(joinPoint);

            // IssueLogEntity 생성 및 저장
            IssueLogEntity logEntity = IssueLogEntity.create(issueEntity, issueLogging.actionType(), slackActorId);
            issueLogEntityRepository.save(logEntity);

            log.info("IssueLog created: issueId={}, actionType={}, slackActorId={}",
                    issueId, issueLogging.actionType(), slackActorId);

        } catch (Exception e) {
            log.error("Failed to of IssueLog", e);
            // 예외를 다시 던지지 않음 - 로그 실패가 비즈니스 로직에 영향 없도록
        }
    }

    private String extractSlackActorId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof IssueRequest request) {
                return request.getRequestUserId();
            }
        }
        throw new IllegalStateException("CreateIssueRequest not found in method arguments");
    }
}
