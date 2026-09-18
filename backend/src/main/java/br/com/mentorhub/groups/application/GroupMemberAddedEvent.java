package br.com.mentorhub.groups.application;

import java.util.UUID;

public record GroupMemberAddedEvent(UUID groupId, UUID actorUserId, UUID memberUserId) {
}
