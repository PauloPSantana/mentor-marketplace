package br.com.mentorhub.studyplans.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataStudyTaskProgressRepository extends JpaRepository<StudyTaskProgressJpaEntity, UUID> {

    Optional<StudyTaskProgressJpaEntity> findByStudyTaskIdAndMenteeUserId(UUID studyTaskId, UUID menteeUserId);

    List<StudyTaskProgressJpaEntity> findByStudyTaskIdInAndMenteeUserId(Collection<UUID> studyTaskIds, UUID menteeUserId);
}
