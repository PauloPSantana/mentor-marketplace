package br.com.mentorhub.notifications.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationTest {

    @Test
    void shouldCreateUnreadNotification() {
        UUID recipientId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();

        Notification notification = Notification.create(
                recipientId,
                actorId,
                NotificationType.POST_LIKED,
                postId,
                null
        );

        assertFalse(notification.isRead());
        assertTrue(notification.isOwnedBy(recipientId));
    }

    @Test
    void shouldRejectSelfNotification() {
        UUID userId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> Notification.create(userId, userId, NotificationType.POST_LIKED, UUID.randomUUID(), null)
        );
    }

    @Test
    void shouldMarkAsReadOnce() {
        Notification notification = Notification.create(
                UUID.randomUUID(),
                UUID.randomUUID(),
                NotificationType.POST_COMMENTED,
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        notification.markAsRead(java.time.Instant.now());
        assertTrue(notification.isRead());

        java.time.Instant firstReadAt = notification.getReadAt();
        notification.markAsRead(java.time.Instant.now().plusSeconds(60));
        assertEquals(firstReadAt, notification.getReadAt());
    }
}
