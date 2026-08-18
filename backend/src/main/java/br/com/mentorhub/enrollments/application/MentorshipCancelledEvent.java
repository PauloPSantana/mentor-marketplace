package br.com.mentorhub.enrollments.application;

import java.util.UUID;

public record MentorshipCancelledEvent(UUID enrollmentId, UUID mentorUserId, UUID menteeUserId) {
}
