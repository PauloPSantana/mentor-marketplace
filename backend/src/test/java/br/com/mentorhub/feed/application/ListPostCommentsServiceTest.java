package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Comment;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.CommentStatus;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPostCommentsServiceTest {

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

    private ListPostCommentsService service;

    @BeforeEach
    void setUp() {
        service = new ListPostCommentsService(
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
    void shouldBuildNestedCommentsTree() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment root = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE");
        Comment reply = Comment.create(postId, root.getId(), UUID.randomUUID(), "Resposta", "João", null, null, "MENTEE");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(postId)).thenReturn(List.of(root, reply));

        var response = service.execute(postId);

        assertEquals(2, response.totalCount());
        assertEquals(1, response.items().size());
        assertEquals(1, response.items().get(0).replies().size());
        assertEquals("Resposta", response.items().get(0).replies().get(0).content());
    }

    @Test
    void shouldShowDeletedPlaceholderInTree() {
        UUID postId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Post", null, "Paulo", null, "Mentor", "MENTOR");
        Comment deletedRoot = Comment.create(postId, null, UUID.randomUUID(), "Comentário", "Ana", null, null, "MENTEE")
                .markAsDeleted();
        Comment reply = Comment.create(postId, deletedRoot.getId(), UUID.randomUUID(), "Resposta", "João", null, null, "MENTEE");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostIdOrderByCreatedAtAsc(postId)).thenReturn(List.of(deletedRoot, reply));

        var response = service.execute(postId);

        assertEquals(1, response.totalCount());
        assertEquals(Comment.DELETED_PLACEHOLDER, response.items().get(0).content());
        assertEquals(CommentStatus.DELETED, response.items().get(0).status());
        assertEquals(1, response.items().get(0).replies().size());
    }
}
