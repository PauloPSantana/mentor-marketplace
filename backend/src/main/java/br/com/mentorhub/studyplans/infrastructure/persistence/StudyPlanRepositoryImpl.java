package br.com.mentorhub.studyplans.infrastructure.persistence;

import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class StudyPlanRepositoryImpl implements StudyPlanRepository {

    private final SpringDataStudyPlanRepository springDataStudyPlanRepository;

    public StudyPlanRepositoryImpl(SpringDataStudyPlanRepository springDataStudyPlanRepository) {
        this.springDataStudyPlanRepository = springDataStudyPlanRepository;
    }

    @Override
    public StudyPlan save(StudyPlan plan) {
        return toDomain(springDataStudyPlanRepository.save(toEntity(plan)));
    }

    @Override
    public Optional<StudyPlan> findById(UUID id) {
        return springDataStudyPlanRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<StudyPlan> findByMentorshipId(UUID mentorshipId) {
        return springDataStudyPlanRepository.findByMentorshipId(mentorshipId).map(this::toDomain);
    }

    private StudyPlanJpaEntity toEntity(StudyPlan plan) {
        StudyPlanJpaEntity entity = new StudyPlanJpaEntity();
        entity.setId(plan.getId());
        entity.setMentorshipId(plan.getMentorshipId());
        entity.setTitle(plan.getTitle());
        entity.setDescription(plan.getDescription());
        entity.setCreatedAt(plan.getCreatedAt());
        entity.setUpdatedAt(plan.getUpdatedAt());
        return entity;
    }

    private StudyPlan toDomain(StudyPlanJpaEntity entity) {
        return StudyPlan.restore(
                entity.getId(),
                entity.getMentorshipId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
