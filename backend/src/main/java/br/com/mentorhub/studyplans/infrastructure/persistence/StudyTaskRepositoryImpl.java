package br.com.mentorhub.studyplans.infrastructure.persistence;

import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StudyTaskRepositoryImpl implements StudyTaskRepository {

    private final SpringDataStudyTaskRepository springDataStudyTaskRepository;

    public StudyTaskRepositoryImpl(SpringDataStudyTaskRepository springDataStudyTaskRepository) {
        this.springDataStudyTaskRepository = springDataStudyTaskRepository;
    }

    @Override
    public StudyTask save(StudyTask task) {
        return toDomain(springDataStudyTaskRepository.save(toEntity(task)));
    }

    @Override
    public Optional<StudyTask> findById(UUID id) {
        return springDataStudyTaskRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<StudyTask> findByStudyPlanIdOrderByOrderNumberAsc(UUID studyPlanId) {
        return springDataStudyTaskRepository.findByStudyPlanIdOrderByOrderNumberAscCreatedAtAsc(studyPlanId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public int nextOrderNumber(UUID studyPlanId) {
        return springDataStudyTaskRepository.findMaxOrderNumber(studyPlanId) + 1;
    }

    @Override
    public void deleteById(UUID id) {
        springDataStudyTaskRepository.deleteById(id);
    }

    private StudyTaskJpaEntity toEntity(StudyTask task) {
        StudyTaskJpaEntity entity = new StudyTaskJpaEntity();
        entity.setId(task.getId());
        entity.setStudyPlanId(task.getStudyPlanId());
        entity.setTitle(task.getTitle());
        entity.setDescription(task.getDescription());
        entity.setTaskType(task.getTaskType());
        entity.setOrderNumber(task.getOrderNumber());
        entity.setDueDate(task.getDueDate());
        entity.setRequired(task.isRequired());
        entity.setResourceTitle(task.getResourceTitle());
        entity.setResourceUrl(task.getResourceUrl());
        entity.setCreatedAt(task.getCreatedAt());
        entity.setUpdatedAt(task.getUpdatedAt());
        return entity;
    }

    private StudyTask toDomain(StudyTaskJpaEntity entity) {
        return StudyTask.restore(
                entity.getId(),
                entity.getStudyPlanId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getTaskType(),
                entity.getOrderNumber(),
                entity.getDueDate(),
                entity.isRequired(),
                entity.getResourceTitle(),
                entity.getResourceUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
