package com.teuida.jikimi.domain.issue.service;

import com.teuida.jikimi.common.TimeProvider;
import com.teuida.jikimi.domain.issue.repository.IssueQueryRepository;
import com.teuida.jikimi.slack.issue.dto.IssueStatsFilter;
import com.teuida.jikimi.slack.issue.dto.IssueStatsRow;
import com.teuida.jikimi.slack.issue.dto.OpenIssueWithUsergroupsRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IssueStatsService {
    private final TimeProvider timeProvider;
    private final IssueQueryRepository issueQueryRepository;

    public IssueStatsRow getIssueStats(IssueStatsFilter filter) {
        return issueQueryRepository.getIssueStats(filter, timeProvider.nowDateTime());
    }

    public List<OpenIssueWithUsergroupsRow> findOpenIssuesWithUsergroups(IssueStatsFilter filter) {
        return issueQueryRepository.findOpenIssuesWithUsergroups(filter, timeProvider.nowDateTime());
    }
}
