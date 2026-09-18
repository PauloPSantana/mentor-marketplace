package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteStudyTaskService {

    private final StudyPlanAccessService studyPlanAccessService;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final GetStudyPlanService getStudyPlanService;

    public DeleteStudyTaskService(
            StudyPlanAccessService studyPlanAccessService,
            StudyPlanRepository studyPlanRepository,
            StudyTaskRepository studyTaskRepository,
            GetStudyPlanService getStudyPlanService
    ) {
        this.studyPlanAccessService = studyPlanAccessService;
        this.studyPlanRepository = studyPlanRepository;
        this.studyTaskRepository = studyTaskRepository;
        this.getStudyPlanService = getStudyPlanService;
    }

    @Transactional
    public StudyPlanResponse execute(UUID actorUserId, UUID mentorshipId, UUID taskId) {
        Mentorship mentorship = studyPlanAccessService.requireMentorAndMutable(actorUserId, mentorshipId);
        StudyPlan plan = studyPlanRepository.findByMentorshipId(mentorship.getId())
                .orElseThrow(() -> new NotFoundException("Plano de estudos não encontrado"));
        StudyTask task = studyTaskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Atividade não encontrada"));
        if (!task.getStudyPlanId().equals(plan.getId())) {
            throw new NotFoundException("Atividade não encontrada");
        }
        studyTaskRepository.deleteById(task.getId());
        return getStudyPlanService.assemble(plan, mentorship);
    }
}
