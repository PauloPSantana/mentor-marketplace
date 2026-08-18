package br.com.mentorhub.notifications.api.dto;

import br.com.mentorhub.notifications.domain.Notification;
import br.com.mentorhub.notifications.domain.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        boolean read,
        Instant createdAt,
        UUID actorUserId,
        String actorName,
        UUID postId,
        UUID commentId,
        String message
) {
    public static NotificationResponse from(Notification notification, String actorName) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getActorUserId(),
                actorName,
                notification.getPostId(),
                notification.getCommentId(),
                buildMessage(actorName, notification.getType())
        );
    }

    public static String buildMessage(String actorName, NotificationType type) {
        return switch (type) {
            case POST_LIKED -> actorName + " curtiu sua publicação";
            case POST_COMMENTED -> actorName + " comentou sua publicação";
            case COMMENT_REPLIED -> actorName + " respondeu seu comentário";
            case USER_FOLLOWED -> actorName + " começou a seguir você";
            case MENTORSHIP_REQUESTED -> actorName + " solicitou sua mentoria";
            case MENTORSHIP_ACCEPTED -> actorName + " aceitou sua solicitação de mentoria";
            case MENTORSHIP_REJECTED -> actorName + " recusou sua solicitação de mentoria";
            case MENTORSHIP_CANCELLED -> actorName + " cancelou a solicitação de mentoria";
            case MENTORSHIP_COMPLETED -> actorName + " concluiu a mentoria";
            case SESSION_CREATED -> actorName + " agendou uma sessão de mentoria";
            case SESSION_CANCELLED -> actorName + " cancelou uma sessão de mentoria";
            case SESSION_COMPLETED -> actorName + " concluiu uma sessão de mentoria";
            case SESSION_NO_SHOW -> actorName + " registrou ausência em uma sessão";
            case SESSION_REMINDER -> "Você tem uma sessão de mentoria em breve";
        };
    }
}
