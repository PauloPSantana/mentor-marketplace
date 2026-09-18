package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.UpdateStudyTaskProgressRequest;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskProgress;
import br.com.mentorhub.studyplans.domain.StudyTaskProgressRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskStatus;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UpdateStudyTaskProgressService {

    private final StudyPlanAccessService studyPlanAccessService;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final StudyTaskProgressRepository studyTaskProgressRepository;
    private final GetStudyPlanService getStudyPlanService;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateStudyTaskProgressService(
            StudyPlanAccessService studyPlanAccessService,
            StudyPlanRepository studyPlanRepository,
            StudyTaskRepository studyTaskRepository,
            StudyTaskProgressRepository studyTaskProgressRepository,
            GetStudyPlanService getStudyPlanService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.studyPlanAccessService = studyPlanAccessService;
        this.studyPlanRepository = studyPlanRepository;
        this.studyTaskRepository = studyTaskRepository;
        this.studyTaskProgressRepository = studyTaskProgressRepository;
        this.getStudyPlanService = getStudyPlanService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public StudyPlanResponse execute(
            UUID actorUserId,
            UUID mentorshipId,
            UUID taskId,
            UpdateStudyTaskProgressRequest request
    ) {
        Mentorship mentorship = studyPlanAccessService.requireMentee(actorUserId, mentorshipId);
        StudyPlan plan = studyPlanRepository.findByMentorshipId(mentorship.getId())
                .orElseThrow(() -> new NotFoundException("Plano de estudos não encontrado"));
        StudyTask task = studyTaskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Atividade não encontrada"));
        if (!task.getStudyPlanId().equals(plan.getId())) {
            throw new NotFoundException("Atividade não encontrada");
        }
        StudyTaskProgress current = studyTaskProgressRepository
                .findByStudyTaskIdAndMenteeUserId(task.getId(), mentorship.getMenteeUserId())
                .orElseGet(() -> StudyTaskProgress.create(task.getId(), mentorship.getMenteeUserId()));
        studyTaskProgressRepository.save(current.changeStatus(request.status()));
        StudyPlanResponse response = getStudyPlanService.assemble(plan, mentorship);
        if (allRequiredCompleted(plan, mentorship) && request.status() == StudyTaskStatus.COMPLETED) {
            eventPublisher.publishEvent(new StudyPlanCompletedEvent(
                    mentorship.getId(),
                    mentorship.getMenteeUserId(),
                    mentorship.getMentorUserId()
            ));
        }
        return response;
    }

    private boolean allRequiredCompleted(StudyPlan plan, Mentorship mentorship) {
        List<StudyTask> tasks = studyTaskRepository.findByStudyPlanIdOrderByOrderNumberAsc(plan.getId()).stream()
                .filter(StudyTask::isRequired)
                .toList();
        if (tasks.isEmpty()) {
            return false;
        }
        Map<UUID, StudyTaskProgress> progress = studyTaskProgressRepository.findByStudyTaskIdInAndMenteeUserId(
                tasks.stream().map(StudyTask::getId).toList(),
                mentorship.getMenteeUserId()
        ).stream().collect(Collectors.toMap(StudyTaskProgress::getStudyTaskId, item -> item, (left, right) -> left));
        return tasks.stream().allMatch(task -> {
            StudyTaskProgress item = progress.get(task.getId());
            return item != null && item.isCompleted();
        });
    }
}
