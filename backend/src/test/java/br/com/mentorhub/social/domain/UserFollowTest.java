package br.com.mentorhub.social.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserFollowTest {

    @Test
    void shouldCreateFollowRelationship() {
        UUID followerId = UUID.randomUUID();
        UUID followedId = UUID.randomUUID();

        UserFollow follow = UserFollow.create(followerId, followedId);

        assertEquals(followerId, follow.getFollowerId());
        assertEquals(followedId, follow.getFollowedId());
    }

    @Test
    void shouldRejectSelfFollow() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> UserFollow.create(userId, userId));
    }
}
