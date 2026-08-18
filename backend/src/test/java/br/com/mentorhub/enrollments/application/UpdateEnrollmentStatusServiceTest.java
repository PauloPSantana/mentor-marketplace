package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.enrollments.domain.EnrollmentStatus;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.application.CreateMentorshipFromAcceptedRequestService;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateEnrollmentStatusServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private CreateMentorshipFromAcceptedRequestService createMentorshipFromAcceptedRequestService;
    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EnrollmentResponseMapper enrollmentResponseMapper;

    private UpdateEnrollmentStatusService service;

    @BeforeEach
    void setUp() {
        service = new UpdateEnrollmentStatusService(
                enrollmentRepository,
                mentorshipProductRepository,
                mentorProfileRepository,
                createMentorshipFromAcceptedRequestService,
                mentorshipRepository,
                userRepository,
                eventPublisher,
                enrollmentResponseMapper
        );
    }

    @Test
    void shouldAcceptPendingEnrollmentAndCreateMentorship() {
        UUID mentorUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"), null);
        MentorshipProduct product = MentorshipProduct.create(
                mentorProfileId,
                "Mentoria Java",
                "mentoria-java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                4,
                10,
                new BigDecimal("100.00")
        );
        MentorProfile profile = restoreProfile(mentorProfileId, mentorUserId);
        User mentor = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), mentorUserId);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(mentorshipProductRepository.findById(enrollment.getMentorshipId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(userRepository.findById(mentorUserId)).thenReturn(Optional.of(mentor));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentResponseMapper.toResponse(any(Enrollment.class))).thenReturn(org.mockito.Mockito.mock(EnrollmentResponse.class));

        service.accept(mentorUserId, enrollment.getId());

        ArgumentCaptor<Enrollment> enrollmentCaptor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(enrollmentCaptor.capture());
        assertEquals(EnrollmentStatus.ACCEPTED, enrollmentCaptor.getValue().getStatus());
        verify(createMentorshipFromAcceptedRequestService).execute(
                enrollment.getId(),
                enrollment.getMenteeUserId(),
                mentorProfileId,
                mentorUserId,
                enrollment.getMentorshipId(),
                mentorUserId
        );
        verify(eventPublisher).publishEvent(any(MentorshipAcceptedEvent.class));
    }

    @Test
    void shouldRejectPendingEnrollment() {
        UUID mentorUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"), null);
        MentorshipProduct product = MentorshipProduct.create(
                mentorProfileId,
                "Mentoria Java",
                "mentoria-java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                4,
                10,
                new BigDecimal("100.00")
        );
        MentorProfile profile = restoreProfile(mentorProfileId, mentorUserId);
        User mentor = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), mentorUserId);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(mentorshipProductRepository.findById(enrollment.getMentorshipId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(userRepository.findById(mentorUserId)).thenReturn(Optional.of(mentor));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentResponseMapper.toResponse(any(Enrollment.class))).thenReturn(org.mockito.Mockito.mock(EnrollmentResponse.class));

        service.reject(mentorUserId, enrollment.getId());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertEquals(EnrollmentStatus.REJECTED, captor.getValue().getStatus());
        verify(mentorshipRepository, never()).save(any());
        verify(eventPublisher).publishEvent(any(MentorshipRejectedEvent.class));
    }

    @Test
    void shouldDenyAcceptFromAnotherMentor() {
        UUID ownerUserId = UUID.randomUUID();
        UUID otherMentorId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("100.00"), null);
        MentorshipProduct product = MentorshipProduct.create(
                mentorProfileId,
                "Mentoria Java",
                "mentoria-java",
                "Descrição",
                "Backend",
                "TODOS",
                4,
                4,
                10,
                new BigDecimal("100.00")
        );
        MentorProfile profile = restoreProfile(mentorProfileId, ownerUserId);
        User otherMentor = restore(User.register("Outro", "outro@email.com", "hash", UserRole.MENTOR), otherMentorId);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(mentorshipProductRepository.findById(enrollment.getMentorshipId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(userRepository.findById(otherMentorId)).thenReturn(Optional.of(otherMentor));

        assertThrows(AccessDeniedException.class, () -> service.accept(otherMentorId, enrollment.getId()));
        verify(enrollmentRepository, never()).save(any());
        verify(mentorshipRepository, never()).save(any());
    }

    private User restore(User user, UUID id) {
        return User.restore(
                id,
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private MentorProfile restoreProfile(UUID id, UUID userId) {
        MentorProfile created = MentorProfile.create(userId);
        return MentorProfile.restore(
                id,
                userId,
                created.getHeadline(),
                created.getBio(),
                created.getYearsExperience(),
                created.getPhotoUrl(),
                created.getLinkedinUrl(),
                created.getGithubUrl(),
                new BigDecimal("100.00"),
                created.getModality(),
                created.getSkills(),
                created.getTechnologies(),
                created.isVerified(),
                created.getRatingAvg(),
                created.getRatingCount(),
                created.isActive(),
                created.getCreatedAt(),
                created.getUpdatedAt()
        );
    }
}
