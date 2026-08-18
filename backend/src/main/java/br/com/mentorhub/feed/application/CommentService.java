package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.CommentRequest;
import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.api.dto.PostCommentsResponse;
import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommentResponse create(UUID postId, UUID authorUserId, CommentRequest request) {
        Post post = requirePost(postId);
        User author = requireUser(authorUserId);
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(authorUserId).orElse(null);

        UUID parentCommentId = request.parentCommentId();
        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new NotFoundException("Comentário pai não encontrado"));
            if (!parentComment.getPostId().equals(post.getId())) {
                throw new BusinessException("INVALID_COMMENT", "Resposta deve pertencer à mesma publicação");
            }
        }

        Comment comment = Comment.create(
                post.getId(),
                parentCommentId,
                author.getId(),
                request.content(),
                author.getName(),
                resolvePhotoUrl(mentorProfile),
                resolveHeadline(author, mentorProfile),
                author.getRole().name()
        );

        Comment saved = commentRepository.save(comment);
        publishCommentNotifications(post, author.getId(), saved, parentComment);
        return CommentResponse.from(saved);
    }

    private void publishCommentNotifications(Post post, UUID authorUserId, Comment saved, Comment parentComment) {
        if (!authorUserId.equals(post.getAuthorUserId())) {
            eventPublisher.publishEvent(new PostCommentedEvent(
                    post.getId(),
                    authorUserId,
                    post.getAuthorUserId(),
                    saved.getId(),
                    false
            ));
        }
        if (parentComment != null && !authorUserId.equals(parentComment.getAuthorUserId())) {
            eventPublisher.publishEvent(new PostCommentedEvent(
                    post.getId(),
                    authorUserId,
                    parentComment.getAuthorUserId(),
                    saved.getId(),
                    true
            ));
        }
    }

    @Transactional(readOnly = true)
    public PostCommentsResponse listByPost(UUID postId) {
        requirePost(postId);
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return new PostCommentsResponse(comments.size(), buildTree(comments));
    }

    @Transactional
    public void delete(UUID postId, UUID commentId, UUID currentUserId) {
        requirePost(postId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentário não encontrado"));
        if (!comment.getPostId().equals(postId)) {
            throw new NotFoundException("Comentário não encontrado");
        }
        if (!comment.isOwnedBy(currentUserId)) {
            throw new AccessDeniedException("Apenas o autor pode excluir o comentário");
        }
        commentRepository.deleteById(comment.getId());
    }

    @Transactional(readOnly = true)
    public long countByPostId(UUID postId) {
        return commentRepository.countByPostId(postId);
    }

    private List<CommentResponse> buildTree(List<Comment> comments) {
        Map<UUID, CommentResponse> byId = new LinkedHashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (Comment comment : comments) {
            byId.put(comment.getId(), CommentResponse.from(comment));
        }

        for (Comment comment : comments) {
            CommentResponse current = byId.get(comment.getId());
            if (comment.getParentCommentId() == null) {
                roots.add(current);
                continue;
            }
            CommentResponse parent = byId.get(comment.getParentCommentId());
            if (parent == null) {
                roots.add(current);
                continue;
            }
            parent.replies().add(current);
        }

        sortRepliesRecursively(roots);
        return roots;
    }

    private void sortRepliesRecursively(List<CommentResponse> items) {
        items.sort(Comparator.comparing(CommentResponse::createdAt));
        for (CommentResponse item : items) {
            if (!item.replies().isEmpty()) {
                sortRepliesRecursively(item.replies());
            }
        }
    }

    private Post requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
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
