package br.com.mentorhub.feed.application;

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
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class CommentComposer {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    CommentComposer(
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

    Post requirePost(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Publicação não encontrada"));
    }

    Comment requireCommentOnPost(UUID postId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentário não encontrado"));
        if (!comment.getPostId().equals(postId)) {
            throw new NotFoundException("Comentário não encontrado");
        }
        return comment;
    }

    User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    Comment persist(Post post, UUID authorUserId, String content, Comment parentComment) {
        User author = requireUser(authorUserId);
        MentorProfile mentorProfile = mentorProfileRepository.findByUserId(author.getId()).orElse(null);
        UUID parentCommentId = parentComment == null ? null : parentComment.getId();

        Comment saved = commentRepository.save(Comment.create(
                post.getId(),
                parentCommentId,
                author.getId(),
                content,
                author.getName(),
                photoUrl(author, mentorProfile),
                headline(author, mentorProfile),
                author.getRole().name()
        ));
        publishNotifications(post, author.getId(), saved, parentComment);
        return saved;
    }

    void validateParentComment(Post post, Comment parentComment) {
        if (!parentComment.getPostId().equals(post.getId())) {
            throw new BusinessException("INVALID_COMMENT", "Resposta deve pertencer à mesma publicação");
        }
        if (parentComment.isDeleted()) {
            throw new BusinessException("INVALID_COMMENT", "Não é possível responder a um comentário removido");
        }
        if (parentComment.isReply()) {
            throw new BusinessException("INVALID_COMMENT", "Respostas só podem ser feitas a comentários de primeiro nível");
        }
    }

    private void publishNotifications(Post post, UUID authorUserId, Comment saved, Comment parentComment) {
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

    private static String photoUrl(User author, MentorProfile mentorProfile) {
        if (author.getPhotoUrl() != null && !author.getPhotoUrl().isBlank()) {
            return author.getPhotoUrl();
        }
        return mentorProfile == null ? null : mentorProfile.getPhotoUrl();
    }

    private static String headline(User author, MentorProfile mentorProfile) {
        if (mentorProfile != null && mentorProfile.getHeadline() != null && !mentorProfile.getHeadline().isBlank()) {
            return mentorProfile.getHeadline();
        }
        return switch (author.getRole()) {
            case MENTOR -> "Mentor";
            case MENTEE -> "Mentorado";
            case INSTITUTION -> "Instituição";
            case ADMIN -> "Admin";
        };
    }
}
