package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementComment;
import br.com.mentorhub.announcements.domain.AnnouncementCommentRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteAnnouncementCommentService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;

    public DeleteAnnouncementCommentService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementCommentRepository announcementCommentRepository
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementCommentRepository = announcementCommentRepository;
    }

    @Transactional
    public void execute(UUID actorUserId, UUID groupId, UUID announcementId, UUID commentId) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado"));
        announcementAccessService.requireVisible(group, announcement, actorUserId);
        AnnouncementComment comment = announcementCommentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comentário não encontrado"));
        if (!comment.getAnnouncementId().equals(announcementId)) {
            throw new NotFoundException("Comentário não encontrado");
        }
        User actor = announcementAccessService.requireUser(actorUserId);
        if (!comment.isOwnedBy(actorUserId) && actor.getRole() != UserRole.ADMIN && !group.isMentor(actorUserId)) {
            throw new AccessDeniedException("Apenas o autor, o mentor ou um administrador pode excluir o comentário");
        }
        if (comment.isDeleted()) {
            return;
        }
        announcementCommentRepository.save(comment.markAsDeleted());
    }
}
