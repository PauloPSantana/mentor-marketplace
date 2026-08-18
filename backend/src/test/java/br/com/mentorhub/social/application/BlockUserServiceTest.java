package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.social.domain.UserBlock;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class BlockUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserBlockRepository userBlockRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    private BlockUserService service;

    @BeforeEach
    void setUp() {
        service = new BlockUserService(userRepository, userBlockRepository, userFollowRepository);
    }

    @Test
    void shouldBlockUserAndRemoveFollows() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        User blocker = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), blockerId);
        User blocked = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), blockedId);

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));
        when(userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(false);

        var response = service.execute(blockerId, blockedId);

        assertTrue(response.blocked());
        verify(userBlockRepository).save(any(UserBlock.class));
        verify(userFollowRepository).deleteByFollowerIdAndFollowedId(blockerId, blockedId);
        verify(userFollowRepository).deleteByFollowerIdAndFollowedId(blockedId, blockerId);
    }

    @Test
    void shouldRejectSelfBlock() {
        UUID userId = UUID.randomUUID();

        BusinessException error = assertThrows(BusinessException.class, () -> service.execute(userId, userId));
        assertEquals("SELF_BLOCK", error.getCode());
        verify(userBlockRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateBlock() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();
        User blocker = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), blockerId);
        User blocked = restore(User.register("Paulo", "paulo@email.com", "hash", UserRole.MENTOR), blockedId);

        when(userRepository.findById(blockerId)).thenReturn(Optional.of(blocker));
        when(userRepository.findById(blockedId)).thenReturn(Optional.of(blocked));
        when(userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.execute(blockerId, blockedId));
        verify(userBlockRepository, never()).save(any());
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
