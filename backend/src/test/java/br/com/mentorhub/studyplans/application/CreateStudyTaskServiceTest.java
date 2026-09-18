package br.com.mentorhub.studyplans.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.studyplans.api.dto.StudyPlanResponse;
import br.com.mentorhub.studyplans.api.dto.UpsertStudyTaskRequest;
import br.com.mentorhub.studyplans.domain.StudyPlan;
import br.com.mentorhub.studyplans.domain.StudyPlanRepository;
import br.com.mentorhub.studyplans.domain.StudyTask;
import br.com.mentorhub.studyplans.domain.StudyTaskRepository;
import br.com.mentorhub.studyplans.domain.StudyTaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateStudyTaskServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private StudyPlanRepository studyPlanRepository;
    @Mock
    private StudyTaskRepository studyTaskRepository;
    @Mock
    private GetStudyPlanService getStudyPlanService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateStudyTaskService service;
    private UUID mentorId;
    private UUID menteeId;
    private Mentorship mentorship;
    private StudyPlan plan;

    @BeforeEach
    void setUp() {
        mentorId = UUID.randomUUID();
        menteeId = UUID.randomUUID();
        mentorship = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
                UUID.randomUUID(),
                mentorId,
                UUID.randomUUID(),
                mentorId
        );
        plan = StudyPlan.create(mentorship.getId(), "Arquitetura de Software", "Fundamentos");
        service = new CreateStudyTaskService(
                new StudyPlanAccessService(userRepository, mentorshipRepository),
                studyPlanRepository,
                studyTaskRepository,
                getStudyPlanService,
                eventPublisher
        );
    }

    @Test
    void mentorCanAddTaskToPlan() {
        User mentor = User.restore(
                mentorId, "Paulo", "paulo@email.com", "hash", UserRole.MENTOR, UserStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentor));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));
        when(studyPlanRepository.findByMentorshipId(mentorship.getId())).thenReturn(Optional.of(plan));
        when(studyTaskRepository.nextOrderNumber(plan.getId())).thenReturn(1);
        when(studyTaskRepository.save(any(StudyTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(getStudyPlanService.assemble(plan, mentorship)).thenReturn(
                new StudyPlanResponse(plan.getId(), mentorship.getId(), plan.getTitle(), plan.getDescription(), 0, 1, 0, List.of(), Instant.now(), Instant.now())
        );

        service.execute(mentorId, mentorship.getId(), new UpsertStudyTaskRequest(
                "Assistir aula sobre SOLID",
                "Assistir antes da sessão",
                StudyTaskType.VIDEO,
                null,
                LocalDate.of(2026, 8, 30),
                true,
                "Introdução ao SOLID",
                "https://example.com/solid"
        ));

        ArgumentCaptor<StudyTask> captor = ArgumentCaptor.forClass(StudyTask.class);
        verify(studyTaskRepository).save(captor.capture());
        assertEquals("Assistir aula sobre SOLID", captor.getValue().getTitle());
        verify(eventPublisher).publishEvent(any(StudyTaskAssignedEvent.class));
    }

    @Test
    void menteeCannotAddTask() {
        User mentee = User.restore(
                menteeId, "João", "joao@email.com", "hash", UserRole.MENTEE, UserStatus.ACTIVE, Instant.now(), Instant.now()
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(mentee));
        when(mentorshipRepository.findById(mentorship.getId())).thenReturn(Optional.of(mentorship));

        assertThrows(
                AccessDeniedException.class,
                () -> service.execute(menteeId, mentorship.getId(), new UpsertStudyTaskRequest(
                        "Tarefa",
                        null,
                        StudyTaskType.EXERCISE,
                        1,
                        null,
                        true,
                        null,
                        null
                ))
        );
    }
}
