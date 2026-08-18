package br.com.mentorhub.social.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;
import java.util.UUID;

public interface SpringDataUserBlockRepository extends JpaRepository<UserBlockJpaEntity, UUID> {

    boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId);

    @Query("""
            SELECT CASE WHEN block.blockerId = :userId THEN block.blockedId ELSE block.blockerId END
            FROM UserBlockJpaEntity block
            WHERE block.blockerId = :userId OR block.blockedId = :userId
            """)
    Set<UUID> findRelatedUserIds(@Param("userId") UUID userId);
}
