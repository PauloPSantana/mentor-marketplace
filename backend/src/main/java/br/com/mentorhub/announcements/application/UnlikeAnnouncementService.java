package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementLikeResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementLikeRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnlikeAnnouncementService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementLikeRepository announcementLikeRepository;

    public UnlikeAnnouncementService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementLikeRepository announcementLikeRepository
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementLikeRepository = announcementLikeRepository;
    }

    @Transactional
    public AnnouncementLikeResponse execute(UUID actorUserId, UUID groupId, UUID announcementId) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado"));
        announcementAccessService.requireVisible(group, announcement, actorUserId);
        announcementLikeRepository.deleteByAnnouncementIdAndUserId(announcementId, actorUserId);
        return new AnnouncementLikeResponse(
                announcementId,
                false,
                announcementLikeRepository.countByAnnouncementId(announcementId)
        );
    }
}
