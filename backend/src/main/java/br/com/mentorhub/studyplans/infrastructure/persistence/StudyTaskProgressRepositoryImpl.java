package br.com.mentorhub.studyplans.infrastructure.persistence;

import br.com.mentorhub.studyplans.domain.StudyTaskProgress;
import br.com.mentorhub.studyplans.domain.StudyTaskProgressRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StudyTaskProgressRepositoryImpl implements StudyTaskProgressRepository {

    private final SpringDataStudyTaskProgressRepository springDataStudyTaskProgressRepository;

    public StudyTaskProgressRepositoryImpl(SpringDataStudyTaskProgressRepository springDataStudyTaskProgressRepository) {
        this.springDataStudyTaskProgressRepository = springDataStudyTaskProgressRepository;
    }

    @Override
    public StudyTaskProgress save(StudyTaskProgress progress) {
        return toDomain(springDataStudyTaskProgressRepository.save(toEntity(progress)));
    }

    @Override
    public Optional<StudyTaskProgress> findByStudyTaskIdAndMenteeUserId(UUID studyTaskId, UUID menteeUserId) {
        return springDataStudyTaskProgressRepository.findByStudyTaskIdAndMenteeUserId(studyTaskId, menteeUserId)
                .map(this::toDomain);
    }

    @Override
    public List<StudyTaskProgress> findByStudyTaskIdInAndMenteeUserId(Collection<UUID> studyTaskIds, UUID menteeUserId) {
        if (studyTaskIds == null || studyTaskIds.isEmpty()) {
            return List.of();
        }
        return springDataStudyTaskProgressRepository.findByStudyTaskIdInAndMenteeUserId(studyTaskIds, menteeUserId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private StudyTaskProgressJpaEntity toEntity(StudyTaskProgress progress) {
        StudyTaskProgressJpaEntity entity = new StudyTaskProgressJpaEntity();
        entity.setId(progress.getId());
        entity.setStudyTaskId(progress.getStudyTaskId());
        entity.setMenteeUserId(progress.getMenteeUserId());
        entity.setStatus(progress.getStatus());
        entity.setStartedAt(progress.getStartedAt());
        entity.setCompletedAt(progress.getCompletedAt());
        entity.setUpdatedAt(progress.getUpdatedAt());
        return entity;
    }

    private StudyTaskProgress toDomain(StudyTaskProgressJpaEntity entity) {
        return StudyTaskProgress.restore(
                entity.getId(),
                entity.getStudyTaskId(),
                entity.getMenteeUserId(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getUpdatedAt()
        );
    }
}
