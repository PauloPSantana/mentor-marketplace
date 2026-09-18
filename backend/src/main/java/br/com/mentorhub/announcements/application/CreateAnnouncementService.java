package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementResponse;
import br.com.mentorhub.announcements.api.dto.CreateAnnouncementRequest;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementAudienceType;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.shared.exception.BusinessException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class CreateAnnouncementService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementMapper announcementMapper;
    private final ApplicationEventPublisher eventPublisher;

    public CreateAnnouncementService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementMapper announcementMapper,
            ApplicationEventPublisher eventPublisher
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementMapper = announcementMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AnnouncementResponse execute(UUID actorUserId, UUID groupId, CreateAnnouncementRequest request) {
        MentorshipGroup group = announcementAccessService.requireMentorAndActive(actorUserId, groupId);
        List<UUID> recipients = normalizeRecipients(group, actorUserId, request);
        Announcement saved = announcementRepository.save(Announcement.publish(
                group.getId(),
                actorUserId,
                request.title(),
                request.body(),
                request.audienceType(),
                recipients
        ));
        List<UUID> notify = request.audienceType() == AnnouncementAudienceType.GROUP
                ? group.getMembers().stream().map(member -> member.getUserId()).toList()
                : recipients;
        eventPublisher.publishEvent(new AnnouncementPublishedEvent(saved.getId(), group.getId(), actorUserId, notify));
        return announcementMapper.toResponse(saved, 0, false, 0);
    }

    private List<UUID> normalizeRecipients(
            MentorshipGroup group,
            UUID actorUserId,
            CreateAnnouncementRequest request
    ) {
        if (request.audienceType() != AnnouncementAudienceType.MEMBERS) {
            return List.of();
        }
        Set<UUID> unique = new LinkedHashSet<>();
        if (request.recipientUserIds() != null) {
            unique.addAll(request.recipientUserIds());
        }
        unique.remove(null);
        unique.remove(actorUserId);
        for (UUID userId : unique) {
            if (!group.isMember(userId)) {
                throw new BusinessException("INVALID_ANNOUNCEMENT_AUDIENCE", "Só é possível enviar a integrantes do grupo");
            }
        }
        return new ArrayList<>(unique);
    }
}
