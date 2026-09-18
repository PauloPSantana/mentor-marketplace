package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskProgressRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetStudyPlanService {

    private final StudyPlanAccessService studyPlanAccessService;
    private final StudyPlanRepository studyPlanRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final StudyTaskProgressRepository studyTaskProgressRepository;
    private final StudyPlanMapper studyPlanMapper;

    public GetStudyPlanService(
            StudyPlanAccessService studyPlanAccessService,
            StudyPlanRepository studyPlanRepository,
            StudyTaskRepository studyTaskRepository,
            StudyTaskProgressRepository studyTaskProgressRepository,
            StudyPlanMapper studyPlanMapper
    ) {
        this.studyPlanAccessService = studyPlanAccessService;
        this.studyPlanRepository = studyPlanRepository;
        this.studyTaskRepository = studyTaskRepository;
        this.studyTaskProgressRepository = studyTaskProgressRepository;
        this.studyPlanMapper = studyPlanMapper;
    }

    @Transactional(readOnly = true)
    public Optional<StudyPlanResponse> execute(UUID actorUserId, UUID mentorshipId) {
        Mentorship mentorship = studyPlanAccessService.requireParticipant(actorUserId, mentorshipId);
        return studyPlanRepository.findByMentorshipId(mentorship.getId())
                .map(plan -> StudyPlanAssembler.assemble(
                        plan,
                        mentorship,
                        studyTaskRepository,
                        studyTaskProgressRepository,
                        studyPlanMapper
                ));
    }

    StudyPlanResponse assemble(StudyPlan plan, Mentorship mentorship) {
        return StudyPlanAssembler.assemble(
                plan,
                mentorship,
                studyTaskRepository,
                studyTaskProgressRepository,
                studyPlanMapper
        );
    }
}
