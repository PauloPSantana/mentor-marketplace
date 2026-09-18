package br.com.mentorhub.announcements.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnouncementRepository {

    Announcement save(Announcement announcement);

    Optional<Announcement> findById(UUID id);

    List<Announcement> findByGroupIdOrderByCreatedAtDesc(UUID groupId);
}
