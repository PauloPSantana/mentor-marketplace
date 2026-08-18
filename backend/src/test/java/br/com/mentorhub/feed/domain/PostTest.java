package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostTest {

    @Test
    void shouldPublishWithTrimmedContentAndAuthorSnapshot() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(
                authorId,
                "  Como a mentoria pode acelerar sua carreira?  ",
                " https://cdn.example.com/post.png ",
                "Paulo Santana",
                "https://cdn.example.com/paulo.png",
                "Engenheiro de Software",
                "MENTOR"
        );

        assertEquals("Como a mentoria pode acelerar sua carreira?", post.getContent());
        assertEquals("https://cdn.example.com/post.png", post.getImageUrl());
        assertEquals("Paulo Santana", post.getAuthorName());
        assertEquals(authorId, post.getAuthorUserId());
        assertTrue(post.isOwnedBy(authorId));
        assertFalse(post.isOwnedBy(UUID.randomUUID()));
        assertEquals(post.getCreatedAt(), post.getUpdatedAt());
    }

    @Test
    void shouldRejectBlankContent() {
        assertThrows(
                BusinessException.class,
                () -> Post.publish(UUID.randomUUID(), "   ", null, "Ana", null, null, "MENTEE")
        );
    }

    @Test
    void shouldUpdateContentAndImage() {
        Post post = Post.publish(UUID.randomUUID(), "Texto original", null, "Ana", null, "Mentorado", "MENTEE");

        post.update("Texto editado", "https://cdn.example.com/nova.png");

        assertEquals("Texto editado", post.getContent());
        assertEquals("https://cdn.example.com/nova.png", post.getImageUrl());
    }

    @Test
    void shouldClearBlankImageUrl() {
        Post post = Post.publish(
                UUID.randomUUID(),
                "Texto",
                "https://cdn.example.com/a.png",
                "Ana",
                null,
                null,
                "MENTEE"
        );

        post.update("Texto", "  ");

        assertNull(post.getImageUrl());
    }
}
