package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.identity.domain.UserStatus;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCommentServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private DeleteCommentService service;

    @BeforeEach
    void setUp() {
        service = new DeleteCommentService(
                new CommentComposer(
                        postRepository,
                        commentRepository,
                        userRepository,
                        mentorProfileRepository,
                        eventPublisher
                ),
                commentRepository
        );
    }

    @Test
    void shouldRejectDeleteFromOtherUser() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment comment = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE");
        User otherUser = User.register("Outro", "outro@email.com", "hash", UserRole.MENTEE);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userRepository.findById(otherUser.getId())).thenReturn(Optional.of(otherUser));

        assertThrows(AccessDeniedException.class, () -> service.execute(postId, comment.getId(), otherUser.getId()));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void shouldSoftDeleteCommentFromOwner() {
        UUID postId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment comment = Comment.create(postId, null, ownerId, "Comentário", "Ana", null, null, "MENTEE");
        User owner = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), ownerId);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(postId, comment.getId(), ownerId);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
    }

    @Test
    void shouldAllowAdminToDeleteComment() {
        UUID postId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment comment = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE");
        User admin = User.restore(
                adminId,
                "Admin",
                "admin@email.com",
                "hash",
                UserRole.ADMIN,
                UserStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute(postId, comment.getId(), adminId);

        verify(commentRepository).save(any(Comment.class));
    }

    private User restore(User user, UUID id) {
        return User.restore(
                id,
                user.getName(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
