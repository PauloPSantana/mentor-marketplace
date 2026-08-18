package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPostServiceTest {

    @Mock
    private PostRepository postRepository;

    private GetPostService service;

    @BeforeEach
    void setUp() {
        service = new GetPostService(postRepository);
    }

    @Test
    void shouldReturnExistingPost() {
        Post post = Post.publish(UUID.randomUUID(), "Texto", null, "Paulo", null, "Mentor", "MENTOR");
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        Post found = service.execute(post.getId());

        assertEquals(post.getId(), found.getId());
        assertEquals("Texto", found.getContent());
    }

    @Test
    void shouldRejectMissingPost() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.execute(postId));
    }
}
