package br.com.mentorhub.social.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SpringDataUserFollowRepository extends JpaRepository<UserFollowJpaEntity, UUID> {

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    Optional<UserFollowJpaEntity> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    long countByFollowedId(UUID followedId);

    long countByFollowerId(UUID followerId);

    Page<UserFollowJpaEntity> findByFollowedIdOrderByCreatedAtDesc(UUID followedId, Pageable pageable);

    Page<UserFollowJpaEntity> findByFollowerIdOrderByCreatedAtDesc(UUID followerId, Pageable pageable);

    @Query("""
            SELECT follow.followedId
            FROM UserFollowJpaEntity follow
            WHERE follow.followerId = :followerId
              AND follow.followedId IN :candidateIds
            """)
    Set<UUID> findFollowedIds(@Param("followerId") UUID followerId, @Param("candidateIds") Collection<UUID> candidateIds);

    List<UserFollowJpaEntity> findByFollowerId(UUID followerId);
}
