package com.teuida.jikimi.domain.issue.model;

import com.teuida.jikimi.common.enums.ApplicationType;
import com.teuida.jikimi.common.enums.CourseType;
import com.teuida.jikimi.common.enums.Environment;
import com.teuida.jikimi.common.enums.IssueStatus;
import com.teuida.jikimi.domain.issue.entity.IssueApplicationEntity;
import com.teuida.jikimi.domain.issue.entity.IssueCourseEntity;
import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import com.teuida.jikimi.domain.issue.entity.IssueUsergroupEntity;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Issue {

    // 공통 식별자 및 상태
    private final Long id;
    private final IssueStatus status;

    // 비즈니스 정보
    private final Environment environment;
    private final String title;
    private final String description;
    private final String userEmail;

    /**
     * -- GETTER -- 불변 컬렉션 반환
     */
    @Builder.Default
    private final Set<ApplicationType> applicationTypes = Collections.emptySet();

    @Builder.Default
    private final Set<CourseType> courseTypes = Collections.emptySet();

    // 플랫폼별 컨텍스트
    private final SlackContext slackContext;
    private final JiraContext jiraContext;

    /**
     * Entity와 관련 엔티티들로부터 도메인 객체 생성
     */
    public static Issue of(IssueEntity issueEntity,
                           Set<IssueApplicationEntity> applicationEntities,
                           Set<IssueCourseEntity> courseEntities,
                           Set<IssueUsergroupEntity> issueUsergroupEntities) {
        return Issue.builder()
                .id(issueEntity.getId())
                .status(issueEntity.getStatus())
                .environment(issueEntity.getEnvironment())
                .title(issueEntity.getTitle())
                .description(issueEntity.getDescription())
                .userEmail(issueEntity.getUserEmail())
                .applicationTypes(applicationEntities.stream()
                        .map(IssueApplicationEntity::getType)
                        .collect(Collectors.toSet()))
                .courseTypes(courseEntities.stream()
                        .map(IssueCourseEntity::getType)
                        .collect(Collectors.toSet()))
                .slackContext(SlackContext.builder()
                        .channelId(issueEntity.getSlackChannelId())
                        .messageTs(issueEntity.getSlackMessageTs())
                        .reporterId(issueEntity.getSlackReporterId())
                        .assigneeId(issueEntity.getSlackAssigneeId())
                        .assignedUsergroupIds(issueUsergroupEntities.stream()
                                .map(IssueUsergroupEntity::getSlackUsergroupId)
                                .collect(Collectors.toSet()))
                        .build())
                .jiraContext(JiraContext.builder()
                        .issueKey(issueEntity.getJiraIssueKey())
                        .issueBrowserUrl(issueEntity.getJiraIssueBrowserUrl())
                        .assigneeId(issueEntity.getJiraAssigneeId())
                        .build())
                .build();
    }

    /**
     * Entity로부터 도메인 객체 생성 (관계 엔티티 제외)
     */
    public static Issue from(IssueEntity issueEntity) {
        return Issue.builder()
                .id(issueEntity.getId())
                .status(issueEntity.getStatus())
                .environment(issueEntity.getEnvironment())
                .title(issueEntity.getTitle())
                .description(issueEntity.getDescription())
                .userEmail(issueEntity.getUserEmail())
                .slackContext(SlackContext.builder()
                        .channelId(issueEntity.getSlackChannelId())
                        .messageTs(issueEntity.getSlackMessageTs())
                        .reporterId(issueEntity.getSlackReporterId())
                        .assigneeId(issueEntity.getSlackAssigneeId())
                        .build())
                .jiraContext(JiraContext.builder()
                        .issueKey(issueEntity.getJiraIssueKey())
                        .issueBrowserUrl(issueEntity.getJiraIssueBrowserUrl())
                        .assigneeId(issueEntity.getJiraAssigneeId())
                        .build())
                .build();
    }

    /**
     * Slack 스레드 URL 조회 (편의 메서드)
     */
    public String getSlackThreadUrl(String workspaceUrl) {
        return slackContext.getThreadUrl(workspaceUrl);
    }

    // === 비즈니스 로직 메서드 ===

    /**
     * 이슈가 완전히 할당되었는지 확인 (Slack + Jira 모두)
     */
    public boolean isFullyAssigned() {
        return slackContext != null && slackContext.isAssignedTo() && jiraContext != null && jiraContext.hasAssignee();
    }

    public boolean isOnlySlackAssigned() {
        return jiraContext != null && !jiraContext.hasAssignee() && slackContext != null && slackContext.isAssignedTo();
    }

    /**
     * Slack 관련 컨텍스트
     */
    @Getter
    @Builder
    public static class SlackContext {
        private final String channelId;
        private final String messageTs;
        private final String reporterId;
        private final String assigneeId;

        @Builder.Default
        private final Set<String> assignedUsergroupIds = Collections.emptySet();

        /**
         * Slack 스레드 URL 생성
         *
         * @param workspaceUrl Slack 워크스페이스 URL (예: https://teuida.slack.com)
         * @return 스레드 URL
         */
        public String getThreadUrl(String workspaceUrl) {
            if (messageTs == null || channelId == null) {
                throw new IllegalStateException("messageTs와 channelId가 필요합니다.");
            }
            // 포맷: https://{workspace}.slack.com/archives/{channelId}/p{timestamp(점제거)}
            String tsNoDot = this.messageTs.replace(".", "");
            return String.format("%s/archives/%s/p%s", workspaceUrl, this.channelId, tsNoDot);
        }

        /**
         * 사용자에게 할당되었는지 확인
         */
        public boolean isAssignedTo() {
            return assigneeId != null && !assigneeId.isBlank();
        }

        /**
         * 특정 유저그룹이 할당되었는지 확인
         */
        public boolean isAssignedToUsergroup(String usergroupId) {
            return this.assignedUsergroupIds != null &&
                    this.assignedUsergroupIds.contains(usergroupId);
        }

        /**
         * 불변 컬렉션 반환
         */
        public Set<String> getAssignedUsergroupIds() {
            return assignedUsergroupIds == null ? Collections.emptySet() : assignedUsergroupIds;
        }
    }

    /**
     * Jira 관련 컨텍스트
     */
    @Getter
    @Builder
    public static class JiraContext {
        private final String issueKey;
        private final String issueBrowserUrl;
        private final String assigneeId;
        private final String issueType;
        private final String priority;

        /**
         * Jira 티켓이 생성되었는지 확인
         */
        public boolean isCreated() {
            return issueKey != null && !issueKey.isBlank();
        }

        /**
         * Jira 담당자가 할당되었는지 확인
         */
        public boolean hasAssignee() {
            return assigneeId != null && !assigneeId.isBlank();
        }
    }
}