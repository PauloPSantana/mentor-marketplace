package br.com.mentorhub.mentorships.application;

import java.time.Instant;
import java.util.UUID;

public record SessionCreatedEvent(
        UUID sessionId,
        UUID mentorshipId,
        UUID actorUserId,
        UUID mentorUserId,
        UUID menteeUserId,
        Instant scheduledAt
) {
}
