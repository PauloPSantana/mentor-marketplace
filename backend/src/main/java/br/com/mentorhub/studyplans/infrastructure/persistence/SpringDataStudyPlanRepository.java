package br.com.mentorhub.studyplans.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataStudyPlanRepository extends JpaRepository<StudyPlanJpaEntity, UUID> {

    Optional<StudyPlanJpaEntity> findByMentorshipId(UUID mentorshipId);
}
