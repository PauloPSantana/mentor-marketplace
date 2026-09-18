package br.com.mentorhub.announcements.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AnnouncementCommentRepository {

    AnnouncementComment save(AnnouncementComment comment);

    Optional<AnnouncementComment> findById(UUID id);

    List<AnnouncementComment> findByAnnouncementIdOrderByCreatedAtAsc(UUID announcementId);

    Map<UUID, Long> countActiveByAnnouncementIds(Collection<UUID> announcementIds);
}
