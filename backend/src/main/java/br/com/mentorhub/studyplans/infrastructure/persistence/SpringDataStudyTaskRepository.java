package br.com.mentorhub.studyplans.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataStudyTaskRepository extends JpaRepository<StudyTaskJpaEntity, UUID> {

    List<StudyTaskJpaEntity> findByStudyPlanIdOrderByOrderNumberAscCreatedAtAsc(UUID studyPlanId);

    @Query("select coalesce(max(t.orderNumber), 0) from StudyTaskJpaEntity t where t.studyPlanId = :planId")
    int findMaxOrderNumber(@Param("planId") UUID planId);
}
