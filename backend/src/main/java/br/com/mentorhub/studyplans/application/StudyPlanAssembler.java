package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskProgress;
import br.com.mentorhub.studyplans.domain.StudyTaskProgressRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;

import java.util.List;
import java.util.UUID;

public final class StudyPlanAssembler {

    private StudyPlanAssembler() {
    }

    public static StudyPlanResponse assemble(
            StudyPlan plan,
            Mentorship mentorship,
            StudyTaskRepository studyTaskRepository,
            StudyTaskProgressRepository studyTaskProgressRepository,
            StudyPlanMapper mapper
    ) {
        List<StudyTask> tasks = studyTaskRepository.findByStudyPlanIdOrderByOrderNumberAsc(plan.getId());
        List<UUID> taskIds = tasks.stream().map(StudyTask::getId).toList();
        List<StudyTaskProgress> progress = studyTaskProgressRepository.findByStudyTaskIdInAndMenteeUserId(
                taskIds,
                mentorship.getMenteeUserId()
        );
        return mapper.toResponse(plan, tasks, progress);
    }
}
