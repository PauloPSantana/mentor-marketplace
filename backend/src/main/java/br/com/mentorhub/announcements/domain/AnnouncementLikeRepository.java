package br.com.mentorhub.announcements.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AnnouncementLikeRepository {

    AnnouncementLike save(AnnouncementLike like);

    boolean existsByAnnouncementIdAndUserId(UUID announcementId, UUID userId);

    void deleteByAnnouncementIdAndUserId(UUID announcementId, UUID userId);

    long countByAnnouncementId(UUID announcementId);

    Map<UUID, Long> countByAnnouncementIds(Collection<UUID> announcementIds);

    Set<UUID> findLikedAnnouncementIds(UUID userId, Collection<UUID> announcementIds);
}
