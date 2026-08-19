package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MentorProfileRepository mentorProfileRepository;

    private CreatePostService service;

    @BeforeEach
    void setUp() {
        service = new CreatePostService(postRepository, userRepository, mentorProfileRepository);
    }

    @Test
    void shouldCreatePostWithAuthorFromJwtAndMentorSnapshot() {
        UUID userId = UUID.randomUUID();
        User mentor = restore(User.register("Paulo Santana", "paulo@email.com", "hash", UserRole.MENTOR), userId);
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

        Post post = service.execute(userId, "Como a mentoria acelera a carreira?", null);

        assertEquals(userId, post.getAuthorUserId());
        assertEquals("Como a mentoria acelera a carreira?", post.getContent());
        assertEquals("Paulo Santana", post.getAuthorName());
        assertEquals("Engenheiro de Software", post.getAuthorHeadline());
        assertEquals("https://cdn.example.com/paulo.png", post.getAuthorPhotoUrl());
        assertEquals("MENTOR", post.getAuthorRole());
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getAuthorUserId());
    }

    @Test
    void shouldCreateMenteePostWithoutMentorProfile() {
        UUID userId = UUID.randomUUID();
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mentee));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post post = service.execute(userId, "Quero evoluir na carreira", null);

        assertEquals("Ana", post.getAuthorName());
        assertEquals("Mentorado", post.getAuthorHeadline());
        assertEquals("MENTEE", post.getAuthorRole());
        verify(mentorProfileRepository, never()).findByUserId(any());
    }

    @Test
    void shouldUseUserPhotoForMenteePost() {
        UUID userId = UUID.randomUUID();
        User mentee = restore(User.register("Ana", "ana@email.com", "hash", UserRole.MENTEE), userId);
        mentee.updatePhoto("/uploads/profiles/" + userId + ".jpg");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mentee));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Post post = service.execute(userId, "Quero evoluir na carreira", null);

        assertEquals("/uploads/profiles/" + userId + ".jpg", post.getAuthorPhotoUrl());
        verify(mentorProfileRepository, never()).findByUserId(any());
    }

    @Test
    void shouldRejectUnknownUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.execute(userId, "Texto", null));
        verify(postRepository, never()).save(any());
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
