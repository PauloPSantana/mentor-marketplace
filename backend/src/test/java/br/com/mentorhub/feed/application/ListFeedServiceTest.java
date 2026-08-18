package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFeedServiceTest {

    @Mock
    private PostRepository postRepository;

    private ListFeedService service;

    @BeforeEach
    void setUp() {
        service = new ListFeedService(postRepository);
    }

    @Test
    void shouldListFeedNewestFirstWithDefaultPageSize() {
        Post post = Post.publish(UUID.randomUUID(), "Texto", null, "Paulo", null, "Mentor", "MENTOR");
        when(postRepository.findFeed(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1));

        var feed = service.execute(0, 0);

        assertEquals(1, feed.getContent().size());
        assertEquals(post.getId(), feed.getContent().get(0).getId());
        assertEquals(20, feed.getSize());
    }
}
