package br.com.mentorhub.reviews.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.reviews.api.dto.ReviewResponse;
import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListMentorshipReviewsService {

    private final MentorshipRepository mentorshipRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ListMentorshipReviewsService(
            MentorshipRepository mentorshipRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> execute(UUID actorUserId, UUID mentorshipId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!mentorship.isParticipant(actor.getId()) && actor.getRole() != UserRole.ADMIN) {
            throw new AccessDeniedException("Somente os participantes podem ver estas avaliações");
        }
        List<Review> reviews = reviewRepository.findByMentorshipIdOrderByCreatedAtDesc(mentorshipId);
        List<UUID> reviewerIds = reviews.stream().map(Review::getReviewerUserId).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return reviews.stream()
                .map(review -> {
                    User reviewer = usersById.get(review.getReviewerUserId());
                    return ReviewResponse.from(review, reviewer != null ? reviewer.getName() : "Participante");
                })
                .toList();
    }
}
