package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementCommentResponse;
import br.com.mentorhub.announcements.api.dto.AnnouncementCommentsResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementComment;
import br.com.mentorhub.announcements.domain.AnnouncementCommentRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ListAnnouncementCommentsService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;

    public ListAnnouncementCommentsService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementCommentRepository announcementCommentRepository
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementCommentRepository = announcementCommentRepository;
    }

    @Transactional(readOnly = true)
    public AnnouncementCommentsResponse execute(UUID actorUserId, UUID groupId, UUID announcementId) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new NotFoundException("Comunicado não encontrado"));
        announcementAccessService.requireVisible(group, announcement, actorUserId);
        List<AnnouncementComment> comments = announcementCommentRepository.findByAnnouncementIdOrderByCreatedAtAsc(announcementId);
        long activeCount = comments.stream().filter(AnnouncementComment::isActive).count();
        return new AnnouncementCommentsResponse(activeCount, buildTree(comments));
    }

    private List<AnnouncementCommentResponse> buildTree(List<AnnouncementComment> comments) {
        Map<UUID, AnnouncementCommentResponse> byId = new LinkedHashMap<>();
        List<AnnouncementCommentResponse> roots = new ArrayList<>();
        for (AnnouncementComment comment : comments) {
            byId.put(comment.getId(), AnnouncementCommentResponse.from(comment));
        }
        for (AnnouncementComment comment : comments) {
            AnnouncementCommentResponse current = byId.get(comment.getId());
            if (comment.getParentCommentId() == null) {
                roots.add(current);
                continue;
            }
            AnnouncementCommentResponse parent = byId.get(comment.getParentCommentId());
            if (parent == null) {
                roots.add(current);
            } else {
                parent.replies().add(current);
            }
        }
        roots.sort(Comparator.comparing(AnnouncementCommentResponse::createdAt));
        for (AnnouncementCommentResponse root : roots) {
            root.replies().sort(Comparator.comparing(AnnouncementCommentResponse::createdAt));
        }
        return roots;
    }
}
