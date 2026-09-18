package br.com.mentorhub.groups.application;

import java.util.UUID;

public record GroupMemberLeftEvent(UUID groupId, UUID actorUserId, UUID memberUserId, UUID ownerUserId) {
}
