package br.com.mentorhub.announcements.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAnnouncementRepository extends JpaRepository<AnnouncementJpaEntity, UUID> {

    List<AnnouncementJpaEntity> findByGroupIdOrderByCreatedAtDesc(UUID groupId);
}
