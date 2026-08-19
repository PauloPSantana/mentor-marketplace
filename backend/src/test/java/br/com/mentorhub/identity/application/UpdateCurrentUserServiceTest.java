package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UpdateCurrentUserService service;

    @BeforeEach
    void setUp() {
        service = new UpdateCurrentUserService(userRepository);
    }

    @Test
    void shouldRenameCurrentUser() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTOR);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = service.execute(user.getId(), "Paulo Santana");

        assertEquals("Paulo Santana", updated.getName());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUnknownUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.execute(userId, "Novo Nome"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectBlankName() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTEE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        BusinessException error = assertThrows(
                BusinessException.class,
                () -> service.execute(user.getId(), " ")
        );

        assertEquals("INVALID_NAME", error.getCode());
        verify(userRepository, never()).save(any());
    }
}
