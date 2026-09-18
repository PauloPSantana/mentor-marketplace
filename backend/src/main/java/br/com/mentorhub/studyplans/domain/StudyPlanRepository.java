package br.com.mentorhub.studyplans.domain;

import java.util.Optional;
import java.util.UUID;

public interface StudyPlanRepository {

    StudyPlan save(StudyPlan plan);

    Optional<StudyPlan> findById(UUID id);

    Optional<StudyPlan> findByMentorshipId(UUID mentorshipId);
}
