package br.com.mentorhub.groups.application;

import java.util.UUID;

public record GroupCreatedEvent(UUID groupId, UUID actorUserId, UUID mentorUserId) {
}
