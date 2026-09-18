package br.com.mentorhub.announcements.infrastructure.persistence;

import br.com.mentorhub.announcements.domain.AnnouncementCommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataAnnouncementCommentRepository extends JpaRepository<AnnouncementCommentJpaEntity, UUID> {

    List<AnnouncementCommentJpaEntity> findByAnnouncementIdOrderByCreatedAtAsc(UUID announcementId);

    @Query("""
            select c.announcementId as announcementId, count(c.id) as cnt
            from AnnouncementCommentJpaEntity c
            where c.announcementId in :ids and c.status = :status
            group by c.announcementId
            """)
    List<AnnouncementIdCountView> countGroupedByAnnouncementIds(
            @Param("ids") Collection<UUID> ids,
            @Param("status") AnnouncementCommentStatus status
    );
}
