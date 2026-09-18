package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.UpsertStudyTaskRequest;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateStudyTaskService {

    private final StudyPlanAccessService studyPlanAccessService;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final GetStudyPlanService getStudyPlanService;
    private final ApplicationEventPublisher eventPublisher;

    public CreateStudyTaskService(
            StudyPlanAccessService studyPlanAccessService,
            StudyPlanRepository studyPlanRepository,
            StudyTaskRepository studyTaskRepository,
            GetStudyPlanService getStudyPlanService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.studyPlanAccessService = studyPlanAccessService;
        this.studyPlanRepository = studyPlanRepository;
        this.studyTaskRepository = studyTaskRepository;
        this.getStudyPlanService = getStudyPlanService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public StudyPlanResponse execute(UUID actorUserId, UUID mentorshipId, UpsertStudyTaskRequest request) {
        Mentorship mentorship = studyPlanAccessService.requireMentorAndMutable(actorUserId, mentorshipId);
        StudyPlan plan = studyPlanRepository.findByMentorshipId(mentorship.getId())
                .orElseThrow(() -> new NotFoundException("Crie o plano de estudos antes de adicionar atividades"));
        int orderNumber = request.orderNumber() == null
                ? studyTaskRepository.nextOrderNumber(plan.getId())
                : request.orderNumber();
        studyTaskRepository.save(StudyTask.create(
                plan.getId(),
                request.title(),
                request.description(),
                request.taskType(),
                orderNumber,
                request.dueDate(),
                request.required() == null || request.required(),
                request.resourceTitle(),
                request.resourceUrl()
        ));
        eventPublisher.publishEvent(new StudyTaskAssignedEvent(
                mentorship.getId(),
                mentorship.getMenteeUserId(),
                actorUserId,
                plan.getId()
        ));
        return getStudyPlanService.assemble(plan, mentorship);
    }
}
