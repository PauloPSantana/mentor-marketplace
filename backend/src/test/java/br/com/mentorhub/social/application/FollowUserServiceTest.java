package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.social.domain.UserFollow;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private FollowUserService service;

    @BeforeEach
    void setUp() {
        service = new FollowUserService(
                userRepository,
                mentorProfileRepository,
                userFollowRepository,
                eventPublisher
        );
    }

    @Test
    void shouldFollowMentorAndNotify() {
        UUID followerId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        User follower = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        User mentorUser = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        MentorProfile profile = MentorProfile.create(mentorUserId);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(restore(follower, followerId)));
        when(userRepository.findById(mentorUserId)).thenReturn(Optional.of(restore(mentorUser, mentorUserId)));
        when(mentorProfileRepository.findByUserId(mentorUserId)).thenReturn(Optional.of(profile));
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, mentorUserId)).thenReturn(false, true);
        when(userFollowRepository.countByFollowedId(mentorUserId)).thenReturn(1L);
        when(userFollowRepository.countByFollowerId(mentorUserId)).thenReturn(0L);

        var response = service.execute(followerId, mentorUserId);

        assertTrue(response.following());
        assertEquals(1L, response.followerCount());
        verify(userFollowRepository).save(any(UserFollow.class));
        verify(eventPublisher).publishEvent(any(UserFollowedEvent.class));
    }

    @Test
    void shouldRejectSelfFollow() {
        UUID userId = UUID.randomUUID();

        assertThrows(BusinessException.class, () -> service.execute(userId, userId));
        verify(userFollowRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateFollow() {
        UUID followerId = UUID.randomUUID();
        UUID mentorUserId = UUID.randomUUID();
        User follower = User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE);
        User mentorUser = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);
        MentorProfile profile = MentorProfile.create(mentorUserId);

        when(userRepository.findById(followerId)).thenReturn(Optional.of(restore(follower, followerId)));
        when(userRepository.findById(mentorUserId)).thenReturn(Optional.of(restore(mentorUser, mentorUserId)));
        when(mentorProfileRepository.findByUserId(mentorUserId)).thenReturn(Optional.of(profile));
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, mentorUserId)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.execute(followerId, mentorUserId));
        verify(userFollowRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
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
}
