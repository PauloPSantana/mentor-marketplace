package br.com.mentorhub.notifications.application;

import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationRepository;
import br.com.mentorhub.notifications.domain.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    private CreateNotificationService service;

    @BeforeEach
    void setUp() {
        service = new CreateNotificationService(notificationRepository);
    }

    @Test
    void shouldCreateNotificationForAnotherUser() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(recipientId, actorId, NotificationType.POST_LIKED, postId, null);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals(recipientId, captor.getValue().getRecipientUserId());
        assertEquals(actorId, captor.getValue().getActorUserId());
        assertEquals(NotificationType.POST_LIKED, captor.getValue().getType());
    }

    @Test
    void shouldSkipSelfNotification() {
        UUID userId = UUID.randomUUID();

        service.execute(userId, userId, NotificationType.POST_COMMENTED, UUID.randomUUID(), UUID.randomUUID());

        verify(notificationRepository, never()).save(any());
    }
}
