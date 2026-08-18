package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostFeedResponse;
import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    static final int DEFAULT_PAGE_SIZE = 10;
    static final int MAX_PAGE_SIZE = 20;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @Transactional
    public PostResponse create(UUID authorUserId, PostRequest request) {
        User author = requireUser(authorUserId);
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(authorUserId).orElse(null);

        Post post = Post.publish(
                author.getId(),
                request.content(),
                request.imageUrl(),
                author.getName(),
                resolvePhotoUrl(mentorProfile),
                resolveHeadline(author, mentorProfile),
                author.getRole().name()
        );

        return toResponse(postRepository.save(post), authorUserId);
    }

    @Transactional
    public PostResponse update(UUID postId, UUID currentUserId, PostRequest request) {
        Post post = requirePost(postId);
        assertOwner(post, currentUserId);
        post.update(request.content(), request.imageUrl());
        return toResponse(postRepository.save(post), currentUserId);
    }

    @Transactional
    public void delete(UUID postId, UUID currentUserId) {
        Post post = requirePost(postId);
        assertOwner(post, currentUserId);
        postRepository.deleteById(post.getId());
    }

    @Transactional(readOnly = true)
    public PostResponse getById(UUID postId, UUID currentUserId) {
        return toResponse(requirePost(postId), currentUserId);
    }

    @Transactional(readOnly = true)
    public PostFeedResponse listFeed(UUID currentUserId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Page<Post> feed = postRepository.findFeed(PageRequest.of(safePage, safeSize));
        return new PostFeedResponse(
                toResponses(feed.getContent(), currentUserId),
                feed.getNumber(),
                feed.getSize(),
                feed.getTotalElements(),
                feed.getTotalPages(),
                feed.isLast()
        );
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    private Post requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    private void assertOwner(Post post, UUID currentUserId) {
        if (!post.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("Apenas o autor pode alterar esta publicação");
        }
    }

    private PostResponse toResponse(Post post, UUID currentUserId) {
        long likeCount = postLikeRepository.countByPostId(post.getId());
        boolean liked = postLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);
        long commentCount = commentRepository.countByPostId(post.getId());
        return PostResponse.from(post, likeCount, liked, commentCount);
    }

    private List<PostResponse> toResponses(List<Post> posts, UUID currentUserId) {
        return posts.stream()
                .map(post -> toResponse(post, currentUserId))
                .toList();
    }

    private String resolvePhotoUrl(MentorProfile mentorProfile) {
        if (mentorProfile == null) {
            return null;
        }
        return mentorProfile.getPhotoUrl();
    }

    private String resolveHeadline(User author, MentorProfile mentorProfile) {
        if (mentorProfile != null && mentorProfile.getHeadline() != null && !mentorProfile.getHeadline().isBlank()) {
            return mentorProfile.getHeadline();
        }
        return switch (author.getRole()) {
            case MENTOR -> "Mentor";
            case MENTEE -> "Mentorado";
            case ADMIN -> "Admin";
        };
    }
}
