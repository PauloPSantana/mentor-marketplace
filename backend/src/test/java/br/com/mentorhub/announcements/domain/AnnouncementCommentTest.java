package br.com.mentorhub.announcements.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnouncementCommentTest {

    @Test
    void shouldCreateTopLevelComment() {
        UUID announcementId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        AnnouncementComment comment = AnnouncementComment.create(
                announcementId,
                null,
                authorId,
                "  Vou revisar o material  ",
                "João",
                null,
                "MENTEE"
        );

        assertEquals("Vou revisar o material", comment.getContent());
        assertFalse(comment.isReply());
        assertTrue(comment.isOwnedBy(authorId));
        assertTrue(comment.isActive());
    }

    @Test
    void shouldCreateReply() {
        UUID parentId = UUID.randomUUID();
        AnnouncementComment reply = AnnouncementComment.create(
                UUID.randomUUID(),
                parentId,
                UUID.randomUUID(),
                "Perfeito",
                "Paulo",
                null,
                "MENTOR"
        );

        assertTrue(reply.isReply());
        assertEquals(parentId, reply.getParentCommentId());
    }

    @Test
    void shouldSoftDeleteComment() {
        AnnouncementComment comment = AnnouncementComment.create(
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                "Comentário",
                "Ana",
                null,
                "MENTEE"
        );

        AnnouncementComment deleted = comment.markAsDeleted();
        assertTrue(deleted.isDeleted());
        assertEquals(AnnouncementComment.DELETED_PLACEHOLDER, deleted.getDisplayContent());
    }

    @Test
    void shouldRejectBlankContent() {
        assertThrows(
                BusinessException.class,
                () -> AnnouncementComment.create(
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        "  ",
                        "Ana",
                        null,
                        "MENTEE"
                )
        );
    }
}
