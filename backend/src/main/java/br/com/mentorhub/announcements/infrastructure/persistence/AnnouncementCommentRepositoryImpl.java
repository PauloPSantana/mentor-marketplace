package br.com.mentorhub.announcements.infrastructure.persistence;

import br.com.mentorhub.announcements.domain.AnnouncementComment;
import br.com.mentorhub.announcements.domain.AnnouncementCommentRepository;
import br.com.mentorhub.announcements.domain.AnnouncementCommentStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AnnouncementCommentRepositoryImpl implements AnnouncementCommentRepository {

    private final SpringDataAnnouncementCommentRepository springDataAnnouncementCommentRepository;

    public AnnouncementCommentRepositoryImpl(
            SpringDataAnnouncementCommentRepository springDataAnnouncementCommentRepository
    ) {
        this.springDataAnnouncementCommentRepository = springDataAnnouncementCommentRepository;
    }

    @Override
    public AnnouncementComment save(AnnouncementComment comment) {
        return toDomain(springDataAnnouncementCommentRepository.save(toEntity(comment)));
    }

    @Override
    public Optional<AnnouncementComment> findById(UUID id) {
        return springDataAnnouncementCommentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<AnnouncementComment> findByAnnouncementIdOrderByCreatedAtAsc(UUID announcementId) {
        return springDataAnnouncementCommentRepository.findByAnnouncementIdOrderByCreatedAtAsc(announcementId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, Long> countActiveByAnnouncementIds(Collection<UUID> announcementIds) {
        if (announcementIds == null || announcementIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (AnnouncementIdCountView row : springDataAnnouncementCommentRepository.countGroupedByAnnouncementIds(
                announcementIds,
                AnnouncementCommentStatus.ACTIVE
        )) {
            if (row.getAnnouncementId() != null) {
                counts.put(row.getAnnouncementId(), row.getCnt() == null ? 0L : row.getCnt());
            }
        }
        return counts;
    }

    private AnnouncementCommentJpaEntity toEntity(AnnouncementComment comment) {
        AnnouncementCommentJpaEntity entity = new AnnouncementCommentJpaEntity();
        entity.setId(comment.getId());
        entity.setAnnouncementId(comment.getAnnouncementId());
        entity.setParentCommentId(comment.getParentCommentId());
        entity.setAuthorUserId(comment.getAuthorUserId());
        entity.setContent(comment.getContent());
        entity.setAuthorName(comment.getAuthorName());
        entity.setAuthorPhotoUrl(comment.getAuthorPhotoUrl());
        entity.setAuthorRole(comment.getAuthorRole());
        entity.setStatus(comment.getStatus());
        entity.setCreatedAt(comment.getCreatedAt());
        entity.setUpdatedAt(comment.getUpdatedAt());
        return entity;
    }

    private AnnouncementComment toDomain(AnnouncementCommentJpaEntity entity) {
        return AnnouncementComment.restore(
                entity.getId(),
                entity.getAnnouncementId(),
                entity.getParentCommentId(),
                entity.getAuthorUserId(),
                entity.getContent(),
                entity.getAuthorName(),
                entity.getAuthorPhotoUrl(),
                entity.getAuthorRole(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
