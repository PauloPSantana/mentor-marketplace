package br.com.mentorhub.reviews.domain;

import br.com.mentorhub.shared.exception.BusinessException;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Review {

    public static final Duration EDIT_WINDOW = Duration.ofHours(72);
    private static final int MAX_COMMENT = 2000;

    private final UUID id;
    private final UUID mentorshipId;
    private final UUID reviewerUserId;
    private final UUID reviewedUserId;
    private final int rating;
    private final String comment;
    private final ReviewStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;

    private Review(
            UUID id,
            UUID mentorshipId,
            UUID reviewerUserId,
            UUID reviewedUserId,
            int rating,
            String comment,
            ReviewStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id);
        this.mentorshipId = Objects.requireNonNull(mentorshipId);
        this.reviewerUserId = Objects.requireNonNull(reviewerUserId);
        this.reviewedUserId = Objects.requireNonNull(reviewedUserId);
        this.rating = rating;
        this.comment = comment;
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Review create(
            UUID mentorshipId,
            UUID reviewerUserId,
            UUID reviewedUserId,
            int rating,
            String comment
    ) {
        if (reviewerUserId.equals(reviewedUserId)) {
            throw new BusinessException("SELF_REVIEW", "Você não pode avaliar a si mesmo");
        }
        Instant now = Instant.now();
        return new Review(
                UUID.randomUUID(),
                mentorshipId,
                reviewerUserId,
                reviewedUserId,
                requireRating(rating),
                normalizeComment(comment),
                ReviewStatus.ACTIVE,
                now,
                now
        );
    }

    public static Review restore(
            UUID id,
            UUID mentorshipId,
            UUID reviewerUserId,
            UUID reviewedUserId,
            int rating,
            String comment,
            ReviewStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Review(
                id,
                mentorshipId,
                reviewerUserId,
                reviewedUserId,
                rating,
                comment,
                status,
                createdAt,
                updatedAt
        );
    }

    public Review update(int rating, String comment, Instant now) {
        if (status != ReviewStatus.ACTIVE) {
            throw new BusinessException("INVALID_REVIEW_STATUS", "Avaliação não pode ser editada");
        }
        if (now.isAfter(createdAt.plus(EDIT_WINDOW))) {
            throw new BusinessException("REVIEW_EDIT_EXPIRED", "O prazo para editar a avaliação encerrou");
        }
        return restore(
                id,
                mentorshipId,
                reviewerUserId,
                reviewedUserId,
                requireRating(rating),
                normalizeComment(comment),
                status,
                createdAt,
                now
        );
    }

    public Review hide() {
        return restore(
                id, mentorshipId, reviewerUserId, reviewedUserId, rating, comment,
                ReviewStatus.HIDDEN, createdAt, Instant.now()
        );
    }

    public Review remove() {
        return restore(
                id, mentorshipId, reviewerUserId, reviewedUserId, rating, comment,
                ReviewStatus.REMOVED, createdAt, Instant.now()
        );
    }

    public boolean countsForAverage() {
        return status == ReviewStatus.ACTIVE;
    }

    public boolean isOwnedBy(UUID userId) {
        return reviewerUserId.equals(userId);
    }

    private static int requireRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException("INVALID_RATING", "A nota deve ser entre 1 e 5");
        }
        return rating;
    }

    private static String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        String trimmed = comment.trim();
        if (trimmed.length() > MAX_COMMENT) {
            throw new BusinessException("INVALID_REVIEW_COMMENT", "Comentário deve ter no máximo 2000 caracteres");
        }
        return trimmed;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMentorshipId() {
        return mentorshipId;
    }

    public UUID getReviewerUserId() {
        return reviewerUserId;
    }

    public UUID getReviewedUserId() {
        return reviewedUserId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
