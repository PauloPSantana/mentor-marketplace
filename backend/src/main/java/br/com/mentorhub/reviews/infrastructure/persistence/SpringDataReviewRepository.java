package br.com.mentorhub.reviews.infrastructure.persistence;

import br.com.mentorhub.reviews.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataReviewRepository extends JpaRepository<ReviewJpaEntity, UUID> {

    Optional<ReviewJpaEntity> findByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId);

    List<ReviewJpaEntity> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId);

    List<ReviewJpaEntity> findByReviewedUserIdAndStatusOrderByCreatedAtDesc(UUID reviewedUserId, ReviewStatus status);

    boolean existsByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId);
}
