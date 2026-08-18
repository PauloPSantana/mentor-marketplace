package br.com.mentorhub.feed.application;

import java.util.UUID;

public record PostCommentedEvent(
        UUID postId,
        UUID actorUserId,
        UUID recipientUserId,
        UUID commentId,
        boolean replyToComment
) {
}
