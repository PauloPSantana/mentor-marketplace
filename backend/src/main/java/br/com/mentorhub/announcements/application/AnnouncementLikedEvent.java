package br.com.mentorhub.announcements.application;

import java.util.UUID;

public record AnnouncementLikedEvent(
        UUID announcementId,
        UUID actorUserId,
        UUID authorUserId
) {
}
