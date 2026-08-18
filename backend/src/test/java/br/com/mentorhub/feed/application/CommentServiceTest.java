package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.CommentRequest;
import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private br.com.mentorhub.mentors.domain.MentorProfileRepository mentorProfileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CommentService service;

    @BeforeEach
    void setUp() {
        service = new CommentService(
                postRepository,
                commentRepository,
                userRepository,
                mentorProfileRepository,
                eventPublisher
        );
    }

    @Test
    void shouldCreateReplyToComment() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment parent = Comment.create(post.getId(), null, UUID.randomUUID(), "Pergunta", "Ana", null, null, "MENTEE");
        User replier = User.register("João", "joao@email.com", "hash", UserRole.MENTEE);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(userRepository.findById(replier.getId())).thenReturn(Optional.of(replier));
        when(mentorProfileRepository.findByUserId(replier.getId())).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(post.getId(), replier.getId(), new CommentRequest("Resposta", parent.getId()));

        assertEquals("Resposta", response.content());
        assertEquals(parent.getId(), response.parentCommentId());
    }

    @Test
    void shouldBuildNestedCommentsTree() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment root = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE");
        Comment reply = Comment.create(postId, root.getId(), UUID.randomUUID(), "Resposta", "João", null, null, "MENTEE");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(postId)).thenReturn(List.of(root, reply));

        var response = service.listByPost(postId);

        assertEquals(2, response.totalCount());
        assertEquals(1, response.items().size());
        assertEquals(1, response.items().get(0).replies().size());
        assertEquals("Resposta", response.items().get(0).replies().get(0).content());
    }

    @Test
    void shouldRejectDeleteFromOtherUser() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment comment = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));

        assertThrows(AccessDeniedException.class, () -> service.delete(postId, comment.getId(), UUID.randomUUID()));
        verify(commentRepository, never()).deleteById(any());
    }
}
