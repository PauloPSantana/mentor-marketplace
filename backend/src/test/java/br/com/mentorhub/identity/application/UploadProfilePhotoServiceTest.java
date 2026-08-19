package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadProfilePhotoServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProfilePhotoStorage profilePhotoStorage;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UploadProfilePhotoService service;

    @BeforeEach
    void setUp() {
        service = new UploadProfilePhotoService(userRepository, profilePhotoStorage, eventPublisher);
    }

    @Test
    void shouldStorePhotoAndPublishEvent() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTOR);
        byte[] jpeg = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(profilePhotoStorage.save(user.getId(), jpeg, "jpg")).thenReturn("/uploads/profiles/" + user.getId() + ".jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = service.execute(user.getId(), jpeg);

        assertEquals("/uploads/profiles/" + user.getId() + ".jpg", updated.getPhotoUrl());
        ArgumentCaptor<UserPhotoUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(UserPhotoUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(user.getId(), eventCaptor.getValue().userId());
        assertEquals(updated.getPhotoUrl(), eventCaptor.getValue().photoUrl());
    }

    @Test
    void shouldRejectUnknownUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.execute(userId, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}));
        verify(profilePhotoStorage, never()).save(any(), any(), any());
    }

    @Test
    void shouldRejectInvalidFile() {
        User user = User.register("Paulo Teste", "paulo@email.com", "hash", UserRole.MENTEE);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        BusinessException error = assertThrows(BusinessException.class, () -> service.execute(user.getId(), new byte[] {1, 2, 3}));
        assertEquals("INVALID_PHOTO", error.getCode());
        verify(userRepository, never()).save(any());
    }
}
