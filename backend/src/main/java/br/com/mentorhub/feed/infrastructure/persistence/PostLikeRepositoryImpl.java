package br.com.mentorhub.feed.infrastructure.persistence;

import br.com.mentorhub.feed.domain.PostLike;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
public class PostLikeRepositoryImpl implements PostLikeRepository {

    private final SpringDataPostLikeRepository springDataPostLikeRepository;

    public PostLikeRepositoryImpl(SpringDataPostLikeRepository springDataPostLikeRepository) {
        this.springDataPostLikeRepository = springDataPostLikeRepository;
    }

    @Override
    public PostLike save(PostLike like) {
        return toDomain(springDataPostLikeRepository.save(toEntity(like)));
    }

    @Override
    public boolean existsByPostIdAndUserId(UUID postId, UUID userId) {
        return springDataPostLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    @Override
    @Transactional
    public void deleteByPostIdAndUserId(UUID postId, UUID userId) {
        springDataPostLikeRepository.deleteByPostIdAndUserId(postId, userId);
    }

    @Override
    public long countByPostId(UUID postId) {
        return springDataPostLikeRepository.countByPostId(postId);
    }

    @Override
    public Map<UUID, Long> countByPostIds(Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : springDataPostLikeRepository.countGroupedByPostIds(postIds)) {
            counts.put((UUID) row[0], (Long) row[1]);
        }
        return counts;
    }

    @Override
    public Set<UUID> findLikedPostIds(UUID userId, Collection<UUID> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptySet();
        }
        return springDataPostLikeRepository.findLikedPostIds(userId, postIds);
    }

    private PostLikeJpaEntity toEntity(PostLike like) {
        PostLikeJpaEntity entity = new PostLikeJpaEntity();
        entity.setId(like.getId());
        entity.setPostId(like.getPostId());
        entity.setUserId(like.getUserId());
        entity.setCreatedAt(like.getCreatedAt());
        return entity;
    }

    private PostLike toDomain(PostLikeJpaEntity entity) {
        return PostLike.restore(
                entity.getId(),
                entity.getPostId(),
                entity.getUserId(),
                entity.getCreatedAt()
        );
    }
}
