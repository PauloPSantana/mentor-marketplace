package br.com.mentorhub.studyplans.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyTaskRepository {

    StudyTask save(StudyTask task);

    Optional<StudyTask> findById(UUID id);

    List<StudyTask> findByStudyPlanIdOrderByOrderNumberAsc(UUID studyPlanId);

    int nextOrderNumber(UUID studyPlanId);

    void deleteById(UUID id);
}
