package br.com.mentorhub.reviews.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.reviews.api.dto.CreateReviewRequest;
import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReviewServiceTest {

    @Mock
    private MentorshipRepository mentorshipRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CreateReviewService service;

    @BeforeEach
    void setUp() {
        service = new CreateReviewService(
                mentorshipRepository,
                userRepository,
                reviewRepository,
                mentorProfileRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateReviewOnlyAfterMentorshipIsCompleted() {
        UUID menteeId = UUID.randomUUID();
        Mentorship active = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
        );
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(user(menteeId, "Ana", UserRole.MENTEE)));
        when(mentorshipRepository.findById(active.getId())).thenReturn(Optional.of(active));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(menteeId, active.getId(), new CreateReviewRequest(5, "Ótimo"))
        );
        assertEquals("MENTORSHIP_NOT_COMPLETED", error.getCode());
    }

    @Test
    void shouldRefreshMentorAverageFromPublishedReviews() {
        UUID menteeId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        Mentorship completed = Mentorship.start(
                UUID.randomUUID(),
                menteeId,
                UUID.randomUUID(),
                mentorUserId,
                UUID.randomUUID(),
                mentorUserId
        ).complete(mentorUserId);
        MentorProfile profile = MentorProfile.create(mentorUserId);
        when(userRepository.findById(menteeId)).thenReturn(Optional.of(user(menteeId, "Ana", UserRole.MENTEE)));
        when(mentorshipRepository.findById(completed.getId())).thenReturn(Optional.of(completed));
        when(reviewRepository.findByMentorshipIdAndReviewerUserId(completed.getId(), menteeId)).thenReturn(Optional.empty());
        when(reviewRepository.existsByMentorshipIdAndReviewerUserId(completed.getId(), menteeId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mentorProfileRepository.findByUserId(mentorUserId)).thenReturn(Optional.of(profile));
        when(reviewRepository.findPublishedByReviewedUserIdOrderByCreatedAtDesc(mentorUserId)).thenAnswer(invocation -> {
            Review saved = Review.create(completed.getId(), menteeId, mentorUserId, 4, "Boa");
            return List.of(saved);
        });
        when(mentorProfileRepository.save(any(MentorProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(menteeId, completed.getId(), new CreateReviewRequest(4, "Boa"));

        verify(eventPublisher).publishEvent(any(ReviewCreatedEvent.class));
        verify(mentorProfileRepository).save(profile);
        assertEquals(new BigDecimal("4.00"), profile.getRatingAvg());
        assertEquals(1, profile.getRatingCount());
    }

    private User user(UUID id, String name, UserRole role) {
        Instant now = Instant.now();
        return User.restore(id, name, name.toLowerCase() + "@email.com", "hash", role, UserStatus.ACTIVE, now, now);
    }
}
