package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    private PostService service;

    @BeforeEach
    void setUp() {
        service = new PostService(postRepository, postLikeRepository, commentRepository, userRepository, mentorProfileRepository);
    }

    @Test
    void shouldCreatePostWithMentorSnapshot() {
        UUID userId = UUID.randomUUID();
        User mentor = User.register("Paulo Santana", "paulo@email.com", "hash", UserRole.MENTOR);
        MentorProfile profile = MentorProfile.create(userId);
        profile.update(
                "Engenheiro de Software",
                null,
                null,
                "https://cdn.example.com/paulo.png",
                null,
                null,
                null,
                null,
                null,
                null,
                true
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(mentor));
        when(mentorProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(userId, new PostRequest("Como a mentoria acelera a carreira?", null));

        assertEquals("Como a mentoria acelera a carreira?", response.content());
        assertEquals("Paulo Santana", response.authorName());
        assertEquals("Engenheiro de Software", response.authorHeadline());
        assertEquals("https://cdn.example.com/paulo.png", response.authorPhotoUrl());
        assertEquals("MENTOR", response.authorRole());
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

        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(post.getId(), authorId, new PostRequest("Texto editado", "https://img.test/a.png"));

        assertEquals("Texto editado", response.content());
        assertEquals("https://img.test/a.png", response.imageUrl());
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertEquals("Texto editado", captor.getValue().getContent());
    }

    @Test
    void shouldIncludeLikeStateInFeed() {
        UUID viewerId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findFeed(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1));
        when(postLikeRepository.countByPostId(post.getId())).thenReturn(27L);
        when(postLikeRepository.existsByPostIdAndUserId(post.getId(), viewerId)).thenReturn(true);
        when(commentRepository.countByPostId(post.getId())).thenReturn(5L);

        var feed = service.listFeed(viewerId, 0, 10);

        assertEquals(1, feed.items().size());
        assertEquals(27L, feed.items().get(0).likeCount());
        assertTrue(feed.items().get(0).liked());
        assertEquals(5L, feed.items().get(0).commentCount());
    }
}
