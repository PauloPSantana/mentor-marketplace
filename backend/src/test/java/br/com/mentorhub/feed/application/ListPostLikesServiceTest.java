package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPostLikesServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private UserRepository userRepository;

    private ListPostLikesService service;

    @BeforeEach
    void setUp() {
        service = new ListPostLikesService(postRepository, postLikeRepository, userRepository);
    }

    @Test
    void shouldListLikersForAuthor() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");
        User liker = User.register("João Silva", "joao@email.com", "hash", UserRole.MENTEE);
        PostLike like = PostLike.create(post.getId(), liker.getId());

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPostIdOrderByCreatedAtDesc(post.getId())).thenReturn(List.of(like));
        when(userRepository.findAllByIds(List.of(liker.getId()))).thenReturn(List.of(liker));

        var result = service.execute(post.getId(), authorId);

        assertEquals(1, result.size());
        assertEquals("João Silva", result.get(0).name());
        assertEquals(UserRole.MENTEE, result.get(0).role());
    }

    @Test
    void shouldRejectNonAuthor() {
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThrows(AccessDeniedException.class, () -> service.execute(post.getId(), otherUserId));
    }
}
