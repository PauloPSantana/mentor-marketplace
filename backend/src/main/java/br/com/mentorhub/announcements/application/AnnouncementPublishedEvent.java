package br.com.mentorhub.announcements.application;

import java.util.List;
import java.util.UUID;

public record AnnouncementPublishedEvent(
        UUID announcementId,
        UUID groupId,
        UUID actorUserId,
        List<UUID> recipientUserIds
) {
}
