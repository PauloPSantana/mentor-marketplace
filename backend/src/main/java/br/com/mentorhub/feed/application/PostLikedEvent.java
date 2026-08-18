package br.com.mentorhub.feed.application;

import java.util.UUID;

public record PostLikedEvent(UUID postId, UUID actorUserId, UUID recipientUserId) {
}
