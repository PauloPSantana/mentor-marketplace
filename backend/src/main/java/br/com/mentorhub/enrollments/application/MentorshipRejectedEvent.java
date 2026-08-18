package br.com.mentorhub.enrollments.application;

import java.util.UUID;

public record MentorshipRejectedEvent(UUID enrollmentId, UUID mentorUserId, UUID menteeUserId) {
}
