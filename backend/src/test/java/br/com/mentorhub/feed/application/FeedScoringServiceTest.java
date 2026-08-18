package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.mentors.domain.MentorProfile;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FeedScoringServiceTest {

    @Test
    void shouldPrioritizeFollowedAuthorAndMatchingTechnologies() {
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.parse("2026-08-17T12:00:00Z");
        Post post = Post.publish(
                authorId,
                "Java tips",
                null,
                "Maria",
                null,
                "Mentor",
                "MENTOR"
        );

        MentorProfile profile = MentorProfile.create(authorId);
        profile.update(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of("Java"),
                Set.of("Spring"),
                true
        );

        long score = FeedScoringService.score(
                post,
                Set.of(authorId),
                Set.of("backend"),
                Set.of("spring"),
                Map.of(authorId, profile),
                Map.of(post.getId(), 3L),
                Map.of(post.getId(), 2L),
                now
        );

        assertTrue(score >= FeedScoringService.FOLLOW_WEIGHT + FeedScoringService.TECHNOLOGY_WEIGHT);
    }
}
