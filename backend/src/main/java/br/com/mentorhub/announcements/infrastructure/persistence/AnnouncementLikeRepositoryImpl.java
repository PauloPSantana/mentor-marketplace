package br.com.mentorhub.announcements.infrastructure.persistence;

import br.com.mentorhub.announcements.domain.AnnouncementLike;
import br.com.mentorhub.announcements.domain.AnnouncementLikeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Repository
public class AnnouncementLikeRepositoryImpl implements AnnouncementLikeRepository {

    private final SpringDataAnnouncementLikeRepository springDataAnnouncementLikeRepository;

    public AnnouncementLikeRepositoryImpl(SpringDataAnnouncementLikeRepository springDataAnnouncementLikeRepository) {
        this.springDataAnnouncementLikeRepository = springDataAnnouncementLikeRepository;
    }

    @Override
    public AnnouncementLike save(AnnouncementLike like) {
        AnnouncementLikeJpaEntity entity = new AnnouncementLikeJpaEntity();
        entity.setId(like.getId());
        entity.setAnnouncementId(like.getAnnouncementId());
        entity.setUserId(like.getUserId());
        entity.setCreatedAt(like.getCreatedAt());
        AnnouncementLikeJpaEntity saved = springDataAnnouncementLikeRepository.save(entity);
        return AnnouncementLike.restore(saved.getId(), saved.getAnnouncementId(), saved.getUserId(), saved.getCreatedAt());
    }

    @Override
    public boolean existsByAnnouncementIdAndUserId(UUID announcementId, UUID userId) {
        return springDataAnnouncementLikeRepository.existsByAnnouncementIdAndUserId(announcementId, userId);
    }

    @Override
    @Transactional
    public void deleteByAnnouncementIdAndUserId(UUID announcementId, UUID userId) {
        springDataAnnouncementLikeRepository.deleteByAnnouncementIdAndUserId(announcementId, userId);
    }

    @Override
    public long countByAnnouncementId(UUID announcementId) {
        return springDataAnnouncementLikeRepository.countByAnnouncementId(announcementId);
    }

    @Override
    public Map<UUID, Long> countByAnnouncementIds(Collection<UUID> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (AnnouncementIdCountView row : springDataAnnouncementLikeRepository.countGroupedByAnnouncementIds(announcementIds)) {
            if (row.getAnnouncementId() != null) {
                counts.put(row.getAnnouncementId(), row.getCnt() == null ? 0L : row.getCnt());
            }
        }
        return counts;
    }

    @Override
    public Set<UUID> findLikedAnnouncementIds(UUID userId, Collection<UUID> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            return Collections.emptySet();
        }
        return springDataAnnouncementLikeRepository.findLikedAnnouncementIds(userId, announcementIds);
    }
}
