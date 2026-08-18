package br.com.mentorhub.mentorships.application;

import java.util.UUID;

public record SessionReminderEvent(UUID sessionId, UUID mentorUserId, UUID menteeUserId) {
}
