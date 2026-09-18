package br.com.mentorhub.mentorships.application;

import java.time.Instant;
import java.util.UUID;

public record SessionRescheduledEvent(
        UUID sessionId,
        UUID mentorshipId,
        UUID actorUserId,
        UUID mentorUserId,
        UUID menteeUserId,
        Instant scheduledAt
) {
}
