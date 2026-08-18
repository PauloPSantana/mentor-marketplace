package br.com.mentorhub.enrollments.application;

import java.util.UUID;

public record MentorshipAcceptedEvent(UUID enrollmentId, UUID mentorUserId, UUID menteeUserId) {
}
