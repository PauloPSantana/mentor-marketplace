package br.com.mentorhub.reviews.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import br.com.mentorhub.reviews.api.dto.CreateReviewRequest;
import br.com.mentorhub.reviews.api.dto.ReviewResponse;
import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CreateReviewService {

    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateReviewService(
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            ReviewRepository reviewRepository,
            MentorProfileRepository mentorProfileRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReviewResponse execute(UUID actorUserId, UUID mentorshipId, CreateReviewRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId())) {
            throw new AccessDeniedException("Somente os participantes podem avaliar");
        }
        if (mentorship.getStatus() != MentorshipStatus.COMPLETED) {
            throw new BusinessException("MENTORSHIP_NOT_COMPLETED", "A avaliação só é permitida após a conclusão da mentoria");
        }

        Review existing = reviewRepository.findByMentorshipIdAndReviewerUserId(mentorshipId, actor.getId()).orElse(null);
        UUID reviewedUserId = mentorship.isOwnedByMentee(actor.getId())
                ? mentorship.getMentorUserId()
                : mentorship.getMenteeUserId();

        Review saved;
        if (existing != null) {
            saved = reviewRepository.save(existing.update(request.rating(), request.comment(), Instant.now()));
        } else {
            if (reviewRepository.existsByMentorshipIdAndReviewerUserId(mentorshipId, actor.getId())) {
                throw new ConflictException("Você já avaliou esta mentoria");
            }
            saved = reviewRepository.save(Review.create(
                    mentorshipId,
                    actor.getId(),
                    reviewedUserId,
                    request.rating(),
                    request.comment()
            ));
            eventPublisher.publishEvent(new ReviewCreatedEvent(
                    saved.getId(),
                    saved.getReviewerUserId(),
                    saved.getReviewedUserId(),
                    mentorshipId
            ));
        }
        refreshMentorAverage(mentorship.getMentorUserId());
        return ReviewResponse.from(saved, actor.getName());
    }

    private void refreshMentorAverage(UUID mentorUserId) {
        MentorProfile profile = mentorProfileRepository.findByUserId(mentorUserId).orElse(null);
        if (profile == null) {
            return;
        }
        List<Review> published = reviewRepository.findPublishedByReviewedUserIdOrderByCreatedAtDesc(mentorUserId);
        if (published.isEmpty()) {
            profile.refreshRating(BigDecimal.ZERO, 0);
        } else {
            BigDecimal total = published.stream()
                    .map(review -> BigDecimal.valueOf(review.getRating()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = total.divide(BigDecimal.valueOf(published.size()), 2, RoundingMode.HALF_UP);
            profile.refreshRating(avg, published.size());
        }
        mentorProfileRepository.save(profile);
    }
}
