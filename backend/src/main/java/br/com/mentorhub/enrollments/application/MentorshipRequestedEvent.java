package br.com.mentorhub.enrollments.application;

import java.util.UUID;

public record MentorshipRequestedEvent(UUID enrollmentId, UUID mentorUserId, UUID menteeUserId) {
}
