package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UnfollowUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
    @Mock
    private UserBlockRepository userBlockRepository;

    private UnfollowUserService service;

    @BeforeEach
    void setUp() {
        service = new UnfollowUserService(userRepository, userFollowRepository, userBlockRepository);
    }

    @Test
    void shouldUnfollowAndRefreshCounters() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();
        User followed = User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR);

        when(userRepository.findById(followedId)).thenReturn(Optional.of(restore(followed, followedId)));
        when(userFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId)).thenReturn(false);
        when(userFollowRepository.countByFollowedId(followedId)).thenReturn(0L);
        when(userFollowRepository.countByFollowerId(followedId)).thenReturn(0L);
        when(userBlockRepository.existsByBlockerIdAndBlockedId(followerId, followedId)).thenReturn(false);
        when(userBlockRepository.existsByBlockerIdAndBlockedId(followedId, followerId)).thenReturn(false);

        var response = service.execute(followerId, followedId);

        assertFalse(response.following());
        verify(userFollowRepository).deleteByFollowerIdAndFollowedId(followerId, followedId);
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
