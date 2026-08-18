package br.com.mentorhub.social.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserFollowRepository {

    UserFollow save(UserFollow follow);

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    Optional<UserFollow> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    void deleteByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    long countByFollowedId(UUID followedId);

    long countByFollowerId(UUID followerId);

    Page<UserFollow> findByFollowedIdOrderByCreatedAtDesc(UUID followedId, Pageable pageable);

    Page<UserFollow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId, Pageable pageable);

    Set<UUID> findFollowedIds(UUID followerId, Collection<UUID> candidateIds);

    List<UUID> findAllFollowedIdsByFollowerId(UUID followerId);
}
