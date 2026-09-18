package br.com.mentorhub.announcements.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpringDataAnnouncementRecipientRepository extends JpaRepository<AnnouncementRecipientJpaEntity, UUID> {

    List<AnnouncementRecipientJpaEntity> findByAnnouncementId(UUID announcementId);

    List<AnnouncementRecipientJpaEntity> findByAnnouncementIdIn(Collection<UUID> announcementIds);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from AnnouncementRecipientJpaEntity r where r.announcementId = :announcementId")
    void deleteByAnnouncementId(@Param("announcementId") UUID announcementId);
}
