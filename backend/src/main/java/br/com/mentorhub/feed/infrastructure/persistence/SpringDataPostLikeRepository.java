package br.com.mentorhub.feed.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SpringDataPostLikeRepository extends JpaRepository<PostLikeJpaEntity, UUID> {

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    @Modifying
    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    long countByPostId(UUID postId);

    @Query("select l.postId as postId, count(l.id) as cnt from PostLikeJpaEntity l where l.postId in :postIds group by l.postId")
    List<PostIdCountView> countGroupedByPostIds(@Param("postIds") Collection<UUID> postIds);

    @Query("select l.postId from PostLikeJpaEntity l where l.userId = :userId and l.postId in :postIds")
    Set<UUID> findLikedPostIds(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);

    List<PostLikeJpaEntity> findByPostIdOrderByCreatedAtDesc(UUID postId);
}
