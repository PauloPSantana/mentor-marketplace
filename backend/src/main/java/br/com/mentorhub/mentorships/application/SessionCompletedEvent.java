package br.com.mentorhub.mentorships.application;

import java.util.UUID;

public record SessionCompletedEvent(
        UUID sessionId,
        UUID mentorshipId,
        UUID actorUserId,
        UUID mentorUserId,
        UUID menteeUserId
) {
}
