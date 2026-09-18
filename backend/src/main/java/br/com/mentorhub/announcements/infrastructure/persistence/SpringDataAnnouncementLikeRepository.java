package br.com.mentorhub.announcements.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface SpringDataAnnouncementLikeRepository extends JpaRepository<AnnouncementLikeJpaEntity, UUID> {

    boolean existsByAnnouncementIdAndUserId(UUID announcementId, UUID userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    void deleteByAnnouncementIdAndUserId(UUID announcementId, UUID userId);

    long countByAnnouncementId(UUID announcementId);

    @Query("""
            select l.announcementId as announcementId, count(l.id) as cnt
            from AnnouncementLikeJpaEntity l
            where l.announcementId in :ids
            group by l.announcementId
            """)
    List<AnnouncementIdCountView> countGroupedByAnnouncementIds(@Param("ids") Collection<UUID> ids);

    @Query("""
            select l.announcementId
            from AnnouncementLikeJpaEntity l
            where l.userId = :userId and l.announcementId in :ids
            """)
    Set<UUID> findLikedAnnouncementIds(@Param("userId") UUID userId, @Param("ids") Collection<UUID> ids);
}
