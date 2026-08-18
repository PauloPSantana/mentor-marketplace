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
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
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
class CancelEnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EnrollmentResponseMapper enrollmentResponseMapper;

    private CancelEnrollmentService service;

    @BeforeEach
    void setUp() {
        service = new CancelEnrollmentService(
                enrollmentRepository,
                mentorshipProductRepository,
                mentorProfileRepository,
                userRepository,
                eventPublisher,
                enrollmentResponseMapper
        );
    }

    @Test
    void shouldCancelPendingEnrollmentAsMentee() {
        UUID menteeId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), menteeId, new BigDecimal("100.00"), null);
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
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), menteeId);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(mentee));
        when(mentorshipProductRepository.findById(enrollment.getMentorshipId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentResponseMapper.toResponse(any(Enrollment.class))).thenReturn(org.mockito.Mockito.mock(EnrollmentResponse.class));

        service.execute(menteeId, enrollment.getId());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertEquals(EnrollmentStatus.CANCELLED, captor.getValue().getStatus());
        verify(eventPublisher).publishEvent(any(MentorshipCancelledEvent.class));
    }

    @Test
    void shouldDenyCancelFromMentor() {
        UUID menteeId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        Enrollment enrollment = Enrollment.request(UUID.randomUUID(), menteeId, new BigDecimal("100.00"), null);
        User mentor = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), mentorUserId);

        when(enrollmentRepository.findById(enrollment.getId())).thenReturn(Optional.of(enrollment));
        when(userRepository.findById(mentorUserId)).thenReturn(Optional.of(mentor));

        assertThrows(AccessDeniedException.class, () -> service.execute(mentorUserId, enrollment.getId()));
        verify(enrollmentRepository, never()).save(any());
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
