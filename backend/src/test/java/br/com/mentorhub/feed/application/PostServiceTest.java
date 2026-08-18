package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostResponseMapper postResponseMapper;

    private PostService service;

    @BeforeEach
    void setUp() {
        service = new PostService(postRepository, postResponseMapper);
    }

    @Test
    void shouldRejectUpdateFromAnotherUser() {
        UUID authorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        assertThrows(
                AccessDeniedException.class,
                () -> service.update(post.getId(), otherUserId, new PostRequest("Hack", null))
        );
        verify(postRepository, never()).save(any());
    }

    @Test
    void shouldUpdateOwnPost() {
        UUID authorId = UUID.randomUUID();
        Post post = Post.publish(authorId, "Texto", null, "Paulo", null, "Mentor", "MENTOR");
        PostResponse mapped = PostResponse.from(post, 0, false, 0);

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postResponseMapper.toResponse(any(Post.class), any(UUID.class))).thenReturn(mapped);

        var response = service.update(post.getId(), authorId, new PostRequest("Texto editado", "https://img.test/a.png"));

        assertEquals(mapped, response);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertEquals("Texto editado", captor.getValue().getContent());
        assertEquals("https://img.test/a.png", captor.getValue().getImageUrl());
    }
}
