package br.com.mentorhub.enrollments.application;

import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.MentorshipProduct;
import br.com.mentorhub.mentorships.domain.MentorshipProductRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.social.domain.UserBlockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
class RequestEnrollmentServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorshipProductRepository mentorshipProductRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private UserBlockRepository userBlockRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private EnrollmentResponseMapper enrollmentResponseMapper;

    private RequestEnrollmentService service;

    @BeforeEach
    void setUp() {
        service = new RequestEnrollmentService(
                userRepository,
                mentorshipProductRepository,
                mentorProfileRepository,
                enrollmentRepository,
                userBlockRepository,
                eventPublisher,
                enrollmentResponseMapper
        );
    }

    @Test
    void shouldCreatePendingEnrollment() {
        UUID menteeId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), menteeId);
        MentorProfile profile = restoreProfile(mentorProfileId, mentorUserId);
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
                new BigDecimal("150.00")
        );

        when(userRepository.findById(menteeId)).thenReturn(Optional.of(mentee));
        when(mentorshipProductRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(userBlockRepository.existsEitherDirection(menteeId, mentorUserId)).thenReturn(false);
        when(enrollmentRepository.existsOpenByMentorshipIdAndMenteeUserId(product.getId(), menteeId)).thenReturn(false);
        when(enrollmentRepository.countOccupiedSeats(product.getId())).thenReturn(0L);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(enrollmentResponseMapper.toResponse(any(Enrollment.class))).thenReturn(org.mockito.Mockito.mock(EnrollmentResponse.class));

        service.execute(menteeId, product.getId(), new EnrollmentRequest("Quero aprender"));

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        assertEquals(menteeId, captor.getValue().getMenteeUserId());
        assertEquals(product.getId(), captor.getValue().getMentorshipId());
        verify(eventPublisher).publishEvent(any(MentorshipRequestedEvent.class));
    }

    @Test
    void shouldRejectDuplicateOpenEnrollment() {
        UUID menteeId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), menteeId);
        MentorProfile profile = restoreProfile(mentorProfileId, mentorUserId);
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
                new BigDecimal("150.00")
        );

        when(userRepository.findById(menteeId)).thenReturn(Optional.of(mentee));
        when(mentorshipProductRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));
        when(userBlockRepository.existsEitherDirection(menteeId, mentorUserId)).thenReturn(false);
        when(enrollmentRepository.existsOpenByMentorshipIdAndMenteeUserId(product.getId(), menteeId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.execute(menteeId, product.getId(), new EnrollmentRequest(null)));
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectSelfEnrollment() {
        UUID userId = UUID.randomUUID();
        UUID mentorProfileId = UUID.randomUUID();
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), userId);
        MentorProfile profile = restoreProfile(mentorProfileId, userId);
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
                new BigDecimal("150.00")
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(mentee));
        when(mentorshipProductRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(mentorProfileRepository.findById(mentorProfileId)).thenReturn(Optional.of(profile));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(userId, product.getId(), new EnrollmentRequest(null))
        );
        assertEquals("SELF_ENROLLMENT", error.getCode());
    }

    @Test
    void shouldRejectNonMentee() {
        UUID mentorId = UUID.randomUUID();
        User mentor = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), mentorId);

        when(userRepository.findById(mentorId)).thenReturn(Optional.of(mentor));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(mentorId, UUID.randomUUID(), new EnrollmentRequest(null))
        );
        assertEquals("INVALID_ROLE", error.getCode());
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
                new BigDecimal("150.00"),
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
