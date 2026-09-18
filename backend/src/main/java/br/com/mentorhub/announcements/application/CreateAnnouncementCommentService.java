package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementCommentResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementComment;
import br.com.mentorhub.announcements.domain.AnnouncementCommentRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CreateAnnouncementCommentService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateAnnouncementCommentService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementCommentRepository announcementCommentRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementCommentRepository = announcementCommentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AnnouncementCommentResponse execute(
            UUID actorUserId,
            UUID groupId,
            UUID announcementId,
            String content,
            UUID parentCommentId
    ) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        if (!group.isActive()) {
            throw new BusinessException("GROUP_CLOSED", "Este grupo foi encerrado");
        }
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado"));
        announcementAccessService.requireVisible(group, announcement, actorUserId);
        User actor = announcementAccessService.requireUser(actorUserId);
        AnnouncementComment parent = null;
        if (parentCommentId != null) {
            parent = announcementCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new NotFoundException("Comentário não encontrado"));
            if (!parent.getAnnouncementId().equals(announcementId)) {
                throw new NotFoundException("Comentário não encontrado");
            }
            if (parent.isReply()) {
                throw new BusinessException("INVALID_COMMENT", "Só é possível responder um comentário principal");
            }
            if (!parent.isActive()) {
                throw new BusinessException("INVALID_COMMENT", "Não é possível responder um comentário removido");
            }
        }
        AnnouncementComment saved = announcementCommentRepository.save(AnnouncementComment.create(
                announcementId,
                parentCommentId,
                actor.getId(),
                content,
                actor.getName(),
                actor.getPhotoUrl(),
                actor.getRole().name()
        ));
        eventPublisher.publishEvent(new AnnouncementCommentedEvent(
                announcementId,
                actor.getId(),
                announcement.getAuthorUserId(),
                parent == null ? null : parent.getAuthorUserId()
        ));
        return AnnouncementCommentResponse.from(saved);
    }
}
