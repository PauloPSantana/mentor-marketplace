package br.com.mentorhub.reviews.api.dto;

import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewStatus;

import java.time.Instant;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID mentorshipId,
        UUID reviewerId,
        String reviewerName,
        UUID reviewedUserId,
        int rating,
        String comment,
        ReviewStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewResponse from(Review review, String reviewerName) {
        return new ReviewResponse(
                review.getId(),
                review.getMentorshipId(),
                review.getReviewerUserId(),
                reviewerName,
                review.getReviewedUserId(),
                review.getRating(),
                review.getComment(),
                review.getStatus(),
                review.getCreatedAt(),
                review.getUpdatedAt()
        );
    }
}
