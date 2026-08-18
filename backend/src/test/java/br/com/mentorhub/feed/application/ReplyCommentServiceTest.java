package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyCommentServiceTest {

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

    private ReplyCommentService service;

    @BeforeEach
    void setUp() {
        service = new ReplyCommentService(new CommentComposer(
                postRepository,
                commentRepository,
                userRepository,
                mentorProfileRepository,
                eventPublisher
        ));
    }

    @Test
    void shouldCreateReplyToTopLevelComment() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment parent = Comment.create(post.getId(), null, UUID.randomUUID(), "Pergunta", "Ana", null, null, "MENTEE");
        User replier = restore(User.register("João", "joao@email.com", "hash", UserRole.MENTEE), authorId);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(userRepository.findById(replier.getId())).thenReturn(Optional.of(replier));
        when(mentorProfileRepository.findByUserId(replier.getId())).thenReturn(Optional.empty());
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.execute(post.getId(), parent.getId(), replier.getId(), "Resposta");

        assertEquals("Resposta", response.content());
        assertEquals(parent.getId(), response.parentCommentId());
    }

    @Test
    void shouldRejectReplyToReply() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment root = Comment.create(postId, null, UUID.randomUUID(), "Raiz", "Ana", null, null, "MENTEE");
        Comment reply = Comment.create(postId, root.getId(), UUID.randomUUID(), "Resposta", "João", null, null, "MENTEE");
        User replier = User.register("Maria", "maria@email.com", "hash", UserRole.MENTEE);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findById(reply.getId())).thenReturn(Optional.of(reply));

        assertThrows(
                BusinessException.class,
                () -> service.execute(postId, reply.getId(), replier.getId(), "Mais uma")
        );
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
