package br.com.mentorhub.mentorships.application;

import java.util.UUID;

public record MentorshipCompletedEvent(UUID mentorshipId, UUID mentorUserId, UUID menteeUserId) {
}
