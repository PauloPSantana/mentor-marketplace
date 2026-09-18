package br.com.mentorhub.announcements.application;

import java.util.UUID;

public record AnnouncementCommentedEvent(
        UUID announcementId,
        UUID actorUserId,
        UUID authorUserId,
        UUID parentAuthorUserId
) {
}
