package br.com.mentorhub.reviews.infrastructure.persistence;

import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewRepository;
import br.com.mentorhub.reviews.domain.ReviewStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final SpringDataReviewRepository springDataReviewRepository;

    public ReviewRepositoryImpl(SpringDataReviewRepository springDataReviewRepository) {
        this.springDataReviewRepository = springDataReviewRepository;
    }

    @Override
    public Review save(Review review) {
        return toDomain(springDataReviewRepository.save(toEntity(review)));
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return springDataReviewRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Review> findByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId) {
        return springDataReviewRepository.findByMentorshipIdAndReviewerUserId(mentorshipId, reviewerUserId)
                .map(this::toDomain);
    }

    @Override
    public List<Review> findByMentorshipIdOrderByCreatedAtDesc(UUID mentorshipId) {
        return springDataReviewRepository.findByMentorshipIdOrderByCreatedAtDesc(mentorshipId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Review> findPublishedByReviewedUserIdOrderByCreatedAtDesc(UUID reviewedUserId) {
        return springDataReviewRepository
                .findByReviewedUserIdAndStatusOrderByCreatedAtDesc(reviewedUserId, ReviewStatus.ACTIVE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByMentorshipIdAndReviewerUserId(UUID mentorshipId, UUID reviewerUserId) {
        return springDataReviewRepository.existsByMentorshipIdAndReviewerUserId(mentorshipId, reviewerUserId);
    }

    private ReviewJpaEntity toEntity(Review review) {
        ReviewJpaEntity entity = new ReviewJpaEntity();
        entity.setId(review.getId());
        entity.setMentorshipId(review.getMentorshipId());
        entity.setReviewerUserId(review.getReviewerUserId());
        entity.setReviewedUserId(review.getReviewedUserId());
        entity.setRating(review.getRating());
        entity.setComment(review.getComment());
        entity.setStatus(review.getStatus());
        entity.setCreatedAt(review.getCreatedAt());
        entity.setUpdatedAt(review.getUpdatedAt());
        return entity;
    }

    private Review toDomain(ReviewJpaEntity entity) {
        return Review.restore(
                entity.getId(),
                entity.getMentorshipId(),
                entity.getReviewerUserId(),
                entity.getReviewedUserId(),
                entity.getRating(),
                entity.getComment(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
