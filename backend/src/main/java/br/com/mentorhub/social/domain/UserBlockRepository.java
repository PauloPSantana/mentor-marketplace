package br.com.mentorhub.social.domain;

import java.util.Set;
import java.util.UUID;

public interface UserBlockRepository {

    UserBlock save(UserBlock block);

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    boolean existsEitherDirection(UUID userA, UUID userB);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    Set<UUID> findRelatedUserIds(UUID userId);
}
