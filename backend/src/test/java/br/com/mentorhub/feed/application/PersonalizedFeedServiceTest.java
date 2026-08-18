package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.FeedType;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalizedFeedServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserFollowRepository userFollowRepository;

    @Mock
    private MentorProfileRepository mentorProfileRepository;

    private PersonalizedFeedService service;

    @BeforeEach
    void setUp() {
        service = new PersonalizedFeedService(
                postRepository,
                postLikeRepository,
                commentRepository,
                userFollowRepository,
                mentorProfileRepository
        );
    }

    @Test
    void shouldReturnEmptyFollowingFeedWhenUserFollowsNobody() {
        UUID viewerId = UUID.randomUUID();
        when(userFollowRepository.findAllFollowedIdsByFollowerId(viewerId)).thenReturn(List.of());

        var feed = service.execute(viewerId, FeedType.FOLLOWING, 0, 10);

        assertEquals(FeedType.FOLLOWING, feed.type());
        assertTrue(feed.items().isEmpty());
        assertTrue(feed.last());
    }

    @Test
    void shouldListRecentFeedChronologically() {
        UUID viewerId = UUID.randomUUID();
        Post post = Post.publish(UUID.randomUUID(), "Texto", null, "Paulo", null, "Mentor", "MENTOR");

        when(postRepository.findFeed(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(post), PageRequest.of(0, 10), 1));
        when(userFollowRepository.findAllFollowedIdsByFollowerId(viewerId)).thenReturn(List.of());
        when(postLikeRepository.countByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 1L));
        when(postLikeRepository.findLikedPostIds(viewerId, List.of(post.getId()))).thenReturn(Set.of());
        when(commentRepository.countByPostIds(anyCollection())).thenReturn(Map.of(post.getId(), 0L));

        var feed = service.execute(viewerId, FeedType.RECENT, 0, 10);

        assertEquals(FeedType.RECENT, feed.type());
        assertEquals(1, feed.items().size());
        assertEquals("Texto", feed.items().get(0).content());
    }

    @Test
    void shouldRankForYouFeedByScore() {
        UUID viewerId = UUID.randomUUID();
        UUID followedAuthorId = UUID.randomUUID();
        UUID otherAuthorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-17T12:00:00Z");
        Post followedPost = Post.restore(
                UUID.randomUUID(),
                followedAuthorId,
                "Seguido",
                null,
                "Ana",
                null,
                "Mentor",
                "MENTOR",
                now,
                now
        );
        Post otherPost = Post.restore(
                UUID.randomUUID(),
                otherAuthorId,
                "Outro",
                null,
                "João",
                null,
                "Mentor",
                "MENTOR",
                now.plusSeconds(60),
                now.plusSeconds(60)
        );
        Map<UUID, Long> emptyEngagement = Map.of(
                followedPost.getId(), 0L,
                otherPost.getId(), 0L
        );

        long followedScore = FeedScoringService.score(
                followedPost,
                Set.of(followedAuthorId),
                Set.of(),
                Set.of(),
                Map.of(),
                emptyEngagement,
                emptyEngagement,
                now.plusSeconds(120)
        );
        long otherScore = FeedScoringService.score(
                otherPost,
                Set.of(followedAuthorId),
                Set.of(),
                Set.of(),
                Map.of(),
                emptyEngagement,
                emptyEngagement,
                now.plusSeconds(120)
        );
        assertTrue(followedScore > otherScore);

        doReturn(List.of(followedAuthorId)).when(userFollowRepository).findAllFollowedIdsByFollowerId(viewerId);
        when(postRepository.findRecentPosts(PersonalizedFeedService.FOR_YOU_CANDIDATE_LIMIT))
                .thenReturn(List.of(otherPost, followedPost));
        when(mentorProfileRepository.findByUserIdIn(any())).thenReturn(List.of());
        when(postLikeRepository.countByPostIds(anyCollection())).thenReturn(emptyEngagement);
        when(postLikeRepository.findLikedPostIds(any(), anyCollection())).thenReturn(Set.of());
        when(commentRepository.countByPostIds(anyCollection())).thenReturn(emptyEngagement);

        var feed = service.execute(viewerId, FeedType.FOR_YOU, 0, 10);

        assertEquals(FeedType.FOR_YOU, feed.type());
        assertEquals(2, feed.items().size());
        assertEquals("Seguido", feed.items().get(0).content());
        assertTrue(feed.items().get(0).followingAuthor());
        assertTrue(feed.items().get(0).score() > feed.items().get(1).score());
    }
}
