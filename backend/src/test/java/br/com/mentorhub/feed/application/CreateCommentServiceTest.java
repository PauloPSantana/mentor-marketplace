package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCommentServiceTest {

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

    private CreateCommentService service;

    @BeforeEach
    void setUp() {
        service = new CreateCommentService(new CommentComposer(
                postRepository,
                commentRepository,
                userRepository,
                mentorProfileRepository,
                eventPublisher
        ));
    }

    @Test
    void shouldCreateTopLevelCommentFromJwtAuthor() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        User author = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), authorId);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(userRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(mentorProfileRepository.findByUserId(authorId)).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.execute(post.getId(), authorId, "  Excelente conteúdo!  ");

        assertEquals("Excelente conteúdo!", response.content());
        assertEquals(authorId, response.authorUserId());
        assertEquals("Ana", response.authorName());
        assertNull(response.parentCommentId());
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());
        assertEquals(authorId, captor.getValue().getAuthorUserId());
    }

    @Test
    void shouldRejectMissingPost() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.execute(postId, UUID.randomUUID(), "Texto"));
        verify(commentRepository, never()).save(any());
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
