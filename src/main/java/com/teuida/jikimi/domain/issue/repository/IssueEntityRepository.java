package com.teuida.jikimi.domain.issue.repository;

import com.teuida.jikimi.domain.issue.entity.IssueEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IssueEntityRepository extends JpaRepository<IssueEntity, Long> {

    @Query("""
            select i from IssueEntity i
                where i.id != :excludeId
                and i.embeddings is not null
            order by i.id desc
            limit 2000
            """)
    List<IssueEntity> findTop2000WithEmbeddingsExcluding(Long excludeId);
}
