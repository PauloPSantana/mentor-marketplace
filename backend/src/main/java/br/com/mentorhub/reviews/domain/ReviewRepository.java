package br.com.mentorhub.reviews.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID id);

    Optional<Review> findByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId);

    List<Review> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId);

    List<Review> findPublishedByReviewedUserIdOrderByCreatedAtDesc(UUID reviewedUserId);

    boolean existsByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId);
}
