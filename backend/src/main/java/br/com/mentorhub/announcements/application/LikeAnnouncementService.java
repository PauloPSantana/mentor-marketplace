package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementLikeResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementLike;
import br.com.mentorhub.announcements.domain.AnnouncementLikeRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LikeAnnouncementService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementLikeRepository announcementLikeRepository;
    private final ApplicationEventPublisher eventPublisher;

    public LikeAnnouncementService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementLikeRepository announcementLikeRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementLikeRepository = announcementLikeRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AnnouncementLikeResponse execute(UUID actorUserId, UUID groupId, UUID announcementId) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        if (!group.isActive()) {
            throw new BusinessException("GROUP_CLOSED", "Este grupo foi encerrado");
        }
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado"));
        announcementAccessService.requireVisible(group, announcement, actorUserId);
        if (!announcementLikeRepository.existsByAnnouncementIdAndUserId(announcementId, actorUserId)) {
            announcementLikeRepository.save(AnnouncementLike.create(announcementId, actorUserId));
            eventPublisher.publishEvent(new AnnouncementLikedEvent(
                    announcementId,
                    actorUserId,
                    announcement.getAuthorUserId()
            ));
        }
        return new AnnouncementLikeResponse(
                announcementId,
                true,
                announcementLikeRepository.countByAnnouncementId(announcementId)
        );
    }
}
