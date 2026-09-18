package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.UpsertStudyPlanRequest;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpsertStudyPlanService {

    private final StudyPlanAccessService studyPlanAccessService;
    private final StudyPlanRepository studyPlanRepository;
    private final GetStudyPlanService getStudyPlanService;

    public UpsertStudyPlanService(
            StudyPlanAccessService studyPlanAccessService,
            StudyPlanRepository studyPlanRepository,
            GetStudyPlanService getStudyPlanService
    ) {
        this.studyPlanAccessService = studyPlanAccessService;
        this.studyPlanRepository = studyPlanRepository;
        this.getStudyPlanService = getStudyPlanService;
    }

    @Transactional
    public StudyPlanResponse execute(UUID actorUserId, UUID mentorshipId, UpsertStudyPlanRequest request) {
        Mentorship mentorship = studyPlanAccessService.requireMentorAndMutable(actorUserId, mentorshipId);
        StudyPlan saved = studyPlanRepository.findByMentorshipId(mentorship.getId())
                .map(plan -> plan.update(request.title(), request.description()))
                .orElseGet(() -> StudyPlan.create(mentorship.getId(), request.title(), request.description()));
        saved = studyPlanRepository.save(saved);
        return getStudyPlanService.assemble(saved, mentorship);
    }
}
