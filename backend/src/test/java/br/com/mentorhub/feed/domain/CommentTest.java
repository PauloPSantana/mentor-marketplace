package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentTest {

    @Test
    void shouldCreateTopLevelComment() {
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Comment comment = Comment.create(
                postId,
                null,
                authorId,
                "  Excelente conteúdo!  ",
                "João Silva",
                null,
                "Mentorado",
                "MENTEE"
        );

        assertEquals("Excelente conteúdo!", comment.getContent());
        assertEquals(postId, comment.getPostId());
        assertNull(comment.getParentCommentId());
        assertFalse(comment.isReply());
        assertTrue(comment.isOwnedBy(authorId));
        assertTrue(comment.isActive());
    }

    @Test
    void shouldCreateReply() {
        UUID postId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        Comment reply = Comment.create(
                postId,
                parentId,
                UUID.randomUUID(),
                "Concordo!",
                "Ana",
                null,
                null,
                "MENTOR"
        );

        assertTrue(reply.isReply());
        assertEquals(parentId, reply.getParentCommentId());
    }

    @Test
    void shouldRejectBlankContent() {
        assertThrows(
                BusinessException.class,
                () -> Comment.create(UUID.randomUUID(), null, UUID.randomUUID(), "  ", "Ana", null, null, "MENTEE")
        );
    }

    @Test
    void shouldRejectContentLongerThan1000Characters() {
        String longContent = "a".repeat(1001);
        assertThrows(
                BusinessException.class,
                () -> Comment.create(UUID.randomUUID(), null, UUID.randomUUID(), longContent, "Ana", null, null, "MENTEE")
        );
    }

    @Test
    void shouldSoftDeleteComment() {
        Comment comment = Comment.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "Comentário original",
                "Ana",
                null,
                null,
                "MENTEE"
        );

        Comment deleted = comment.markAsDeleted();

        assertTrue(deleted.isDeleted());
        assertEquals(Comment.DELETED_PLACEHOLDER, deleted.getDisplayContent());
        assertNotNull(deleted.getUpdatedAt());
    }
}
