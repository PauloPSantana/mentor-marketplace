package br.com.mentorhub.feed.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Post {

    private final UUID id;
    private final UUID authorUserId;
    private final String content;
    private final String imageUrl;
    private final String authorName;
    private final String authorPhotoUrl;
    private final String authorHeadline;
    private final String authorRole;
    private final Instant createdAt;

    private Post(
            UUID id,
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.authorUserId = Objects.requireNonNull(authorUserId);
        this.content = requireContent(content);
        this.imageUrl = imageUrl;
        this.authorName = requireAuthorName(authorName);
        this.authorPhotoUrl = authorPhotoUrl;
        this.authorHeadline = authorHeadline;
        this.authorRole = Objects.requireNonNull(authorRole);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Post publish(
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole
    ) {
        return new Post(
                UUID.randomUUID(),
                authorUserId,
                content,
                imageUrl,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                Instant.now()
        );
    }

    public static Post restore(
            UUID id,
            UUID authorUserId,
            String content,
            String imageUrl,
            String authorName,
            String authorPhotoUrl,
            String authorHeadline,
            String authorRole,
            Instant createdAt
    ) {
        return new Post(
                id,
                authorUserId,
                content,
                imageUrl,
                authorName,
                authorPhotoUrl,
                authorHeadline,
                authorRole,
                createdAt
        );
    }

    private static String requireContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException("INVALID_POST", "O conteúdo da publicação é obrigatório");
        }
        String trimmed = content.trim();
        if (trimmed.length() > 5000) {
            throw new BusinessException("INVALID_POST", "Conteúdo deve ter no máximo 5000 caracteres");
        }
        return trimmed;
    }

    private static String requireAuthorName(String authorName) {
        if (authorName == null || authorName.isBlank()) {
            throw new BusinessException("INVALID_POST", "Nome do autor é obrigatório");
        }
        return authorName.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorPhotoUrl() {
        return authorPhotoUrl;
    }

    public String getAuthorHeadline() {
        return authorHeadline;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
