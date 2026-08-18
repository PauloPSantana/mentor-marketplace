package br.com.mentorhub.social.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserBlockTest {

    @Test
    void shouldCreateBlockRelationship() {
        UUID blockerId = UUID.randomUUID();
        UUID blockedId = UUID.randomUUID();

        UserBlock block = UserBlock.create(blockerId, blockedId);

        assertEquals(blockerId, block.getBlockerId());
        assertEquals(blockedId, block.getBlockedId());
    }

    @Test
    void shouldRejectSelfBlock() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> UserBlock.create(userId, userId));
    }
}
