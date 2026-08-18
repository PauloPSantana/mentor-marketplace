package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreatePostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public CreatePostService(
            PostRepository postRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @Transactional
    public Post execute(UUID currentUserId, String content, String imageUrl) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        String photoUrl = null;
        String headline = null;
        if (user.getRole() == UserRole.MENTOR) {
            MentorProfile mentorProfile = mentorProfileRepository.findByUserId(user.getId()).orElse(null);
            if (mentorProfile != null) {
                photoUrl = mentorProfile.getPhotoUrl();
                headline = mentorProfile.getHeadline();
            }
        }
        if (headline == null || headline.isBlank()) {
            headline = switch (user.getRole()) {
                case MENTOR -> "Mentor";
                case MENTEE -> "Mentorado";
                case ADMIN -> "Admin";
            };
        }

        Post post = Post.publish(
                user.getId(),
                content,
                imageUrl,
                user.getName(),
                photoUrl,
                headline,
                user.getRole().name()
        );
        return postRepository.save(post);
    }
}
