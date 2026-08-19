package br.com.mentorhub.reviews.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.reviews.api.dto.MentorRatingResponse;
import br.com.mentorhub.reviews.api.dto.ReviewResponse;
import br.com.mentorhub.reviews.domain.Review;
import br.com.mentorhub.reviews.domain.ReviewRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ListMentorReviewsService {

    private final MentorProfileRepository mentorProfileRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ListMentorReviewsService(
            MentorProfileRepository mentorProfileRepository,
            ReviewRepository reviewRepository,
            UserRepository userRepository
    ) {
        this.mentorProfileRepository = mentorProfileRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> execute(UUID mentorProfileId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        List<Review> reviews = reviewRepository.findPublishedByReviewedUserIdOrderByCreatedAtDesc(profile.getUserId());
        List<UUID> reviewerIds = reviews.stream().map(Review::getReviewerUserId).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return reviews.stream()
                .map(review -> {
                    User reviewer = usersById.get(review.getReviewerUserId());
                    return ReviewResponse.from(review, reviewer != null ? reviewer.getName() : "Mentorado");
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public MentorRatingResponse rating(UUID mentorProfileId) {
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        return new MentorRatingResponse(profile.getRatingAvg(), profile.getRatingCount());
    }
}
