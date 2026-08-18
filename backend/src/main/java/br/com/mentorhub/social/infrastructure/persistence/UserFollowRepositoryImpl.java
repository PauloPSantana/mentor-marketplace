package br.com.mentorhub.social.infrastructure.persistence;

import br.com.mentorhub.social.domain.UserFollow;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserFollowRepositoryImpl implements UserFollowRepository {

    private final SpringDataUserFollowRepository springDataUserFollowRepository;

    public UserFollowRepositoryImpl(SpringDataUserFollowRepository springDataUserFollowRepository) {
        this.springDataUserFollowRepository = springDataUserFollowRepository;
    }

    @Override
    public UserFollow save(UserFollow follow) {
        return toDomain(springDataUserFollowRepository.save(toEntity(follow)));
    }

    @Override
    public boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
        return springDataUserFollowRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public Optional<UserFollow> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
        return springDataUserFollowRepository.findByFollowerIdAndFollowedId(followerId, followedId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId) {
        springDataUserFollowRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
    }

    @Override
    public long countByFollowedId(UUID followedId) {
        return springDataUserFollowRepository.countByFollowedId(followedId);
    }

    @Override
    public long countByFollowerId(UUID followerId) {
        return springDataUserFollowRepository.countByFollowerId(followerId);
    }

    @Override
    public Page<UserFollow> findByFollowedIdOrderByCreatedAtDesc(UUID followedId, Pageable pageable) {
        return springDataUserFollowRepository.findByFollowedIdOrderByCreatedAtDesc(followedId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<UserFollow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId, Pageable pageable) {
        return springDataUserFollowRepository.findByFollowerIdOrderByCreatedAtDesc(followerId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Set<UUID> findFollowedIds(UUID followerId, Collection<UUID> candidateIds) {
        if (candidateIds == null || candidateIds.isEmpty()) {
            return Collections.emptySet();
        }
        return springDataUserFollowRepository.findFollowedIds(followerId, candidateIds);
    }

    @Override
    public List<UUID> findAllFollowedIdsByFollowerId(UUID followerId) {
        return springDataUserFollowRepository.findByFollowerId(followerId).stream()
                .map(UserFollowJpaEntity::getFollowedId)
                .toList();
    }

    private UserFollowJpaEntity toEntity(UserFollow follow) {
        UserFollowJpaEntity entity = new UserFollowJpaEntity();
        entity.setId(follow.getId());
        entity.setFollowerId(follow.getFollowerId());
        entity.setFollowedId(follow.getFollowedId());
        entity.setCreatedAt(follow.getCreatedAt());
        return entity;
    }

    private UserFollow toDomain(UserFollowJpaEntity entity) {
        return UserFollow.restore(
                entity.getId(),
                entity.getFollowerId(),
                entity.getFollowedId(),
                entity.getCreatedAt()
        );
    }
}
