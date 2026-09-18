package br.com.mentorhub.announcements.infrastructure.persistence;

import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementAudienceType;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class AnnouncementRepositoryImpl implements AnnouncementRepository {

    private final SpringDataAnnouncementRepository springDataAnnouncementRepository;
    private final SpringDataAnnouncementRecipientRepository springDataAnnouncementRecipientRepository;

    public AnnouncementRepositoryImpl(
            SpringDataAnnouncementRepository springDataAnnouncementRepository,
            SpringDataAnnouncementRecipientRepository springDataAnnouncementRecipientRepository
    ) {
        this.springDataAnnouncementRepository = springDataAnnouncementRepository;
        this.springDataAnnouncementRecipientRepository = springDataAnnouncementRecipientRepository;
    }

    @Override
    @Transactional
    public Announcement save(Announcement announcement) {
        springDataAnnouncementRepository.saveAndFlush(toEntity(announcement));
        springDataAnnouncementRecipientRepository.deleteByAnnouncementId(announcement.getId());
        if (announcement.getAudienceType() == AnnouncementAudienceType.MEMBERS) {
            List<AnnouncementRecipientJpaEntity> recipients = announcement.getRecipientUserIds().stream()
                    .distinct()
                    .map(userId -> new AnnouncementRecipientJpaEntity(UUID.randomUUID(), announcement.getId(), userId))
                    .toList();
            springDataAnnouncementRecipientRepository.saveAllAndFlush(recipients);
        }
        return announcement;
    }

    @Override
    public Optional<Announcement> findById(UUID id) {
        return springDataAnnouncementRepository.findById(id).map(entity -> toDomain(
                entity,
                springDataAnnouncementRecipientRepository.findByAnnouncementId(id)
        ));
    }

    @Override
    public List<Announcement> findByGroupIdOrderByCreatedAtDesc(UUID groupId) {
        List<AnnouncementJpaEntity> entities = springDataAnnouncementRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        if (entities.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = entities.stream().map(AnnouncementJpaEntity::getId).toList();
        Map<UUID, List<AnnouncementRecipientJpaEntity>> recipientsByAnnouncement =
                springDataAnnouncementRecipientRepository.findByAnnouncementIdIn(ids).stream()
                        .collect(Collectors.groupingBy(AnnouncementRecipientJpaEntity::getAnnouncementId));
        return entities.stream()
                .map(entity -> toDomain(entity, recipientsByAnnouncement.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    private Announcement toDomain(AnnouncementJpaEntity entity, List<AnnouncementRecipientJpaEntity> recipients) {
        List<UUID> recipientIds = recipients == null
                ? List.of()
                : recipients.stream()
                .map(AnnouncementRecipientJpaEntity::getUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
        return Announcement.restore(
                entity.getId(),
                entity.getGroupId(),
                entity.getAuthorUserId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getAudienceType(),
                new ArrayList<>(recipientIds),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private AnnouncementJpaEntity toEntity(Announcement announcement) {
        AnnouncementJpaEntity entity = new AnnouncementJpaEntity();
        entity.setId(announcement.getId());
        entity.setGroupId(announcement.getGroupId());
        entity.setAuthorUserId(announcement.getAuthorUserId());
        entity.setTitle(announcement.getTitle());
        entity.setBody(announcement.getBody());
        entity.setAudienceType(announcement.getAudienceType());
        entity.setCreatedAt(announcement.getCreatedAt());
        entity.setUpdatedAt(announcement.getUpdatedAt());
        return entity;
    }
}
