package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikePostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private LikePostService likePostService;
    private UnlikePostService unlikePostService;

    @BeforeEach
    void setUp() {
        likePostService = new LikePostService(postRepository, postLikeRepository, eventPublisher);
        unlikePostService = new UnlikePostService(postRepository, postLikeRepository);
    }

    @Test
    void shouldLikePost() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.publish(userId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.existsByPostIdAndUserId(postId, userId)).thenReturn(false);
        when(postLikeRepository.countByPostId(postId)).thenReturn(1L);

        var response = likePostService.execute(postId, userId);

        assertTrue(response.liked());
        assertEquals(1L, response.likeCount());
        verify(postLikeRepository).save(any(PostLike.class));
    }

    @Test
    void shouldUnlikePost() {
        UUID postId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Post post = Post.publish(userId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postLikeRepository.countByPostId(postId)).thenReturn(0L);

        var response = unlikePostService.execute(postId, userId);

        assertFalse(response.liked());
        assertEquals(0L, response.likeCount());
        verify(postLikeRepository).deleteByPostIdAndUserId(postId, userId);
        verify(postLikeRepository, never()).save(any());
    }
}
