package br.com.mentorhub.feed.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface PostLikeRepository {

    PostLike save(PostLike like);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    void deleteByPostIdAndUserId(UUID postId, UUID userId);

    long countByPostId(UUID postId);

    Map<UUID, Long> countByPostIds(Collection<UUID> postIds);

    Set<UUID> findLikedPostIds(UUID userId, Collection<UUID> postIds);

    List<PostLike> findByPostIdOrderByCreatedAtDesc(UUID postId);
}
