package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.announcements.domain.AnnouncementCommentRepository;
import br.com.mentorhub.announcements.domain.AnnouncementLikeRepository;
import br.com.mentorhub.announcements.domain.AnnouncementRepository;
import br.com.mentorhub.groups.domain.MentorshipGroup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ListAnnouncementsService {

    private final AnnouncementAccessService announcementAccessService;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementLikeRepository announcementLikeRepository;
    private final AnnouncementCommentRepository announcementCommentRepository;
    private final AnnouncementMapper announcementMapper;

    public ListAnnouncementsService(
            AnnouncementAccessService announcementAccessService,
            AnnouncementRepository announcementRepository,
            AnnouncementLikeRepository announcementLikeRepository,
            AnnouncementCommentRepository announcementCommentRepository,
            AnnouncementMapper announcementMapper
    ) {
        this.announcementAccessService = announcementAccessService;
        this.announcementRepository = announcementRepository;
        this.announcementLikeRepository = announcementLikeRepository;
        this.announcementCommentRepository = announcementCommentRepository;
        this.announcementMapper = announcementMapper;
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> execute(UUID actorUserId, UUID groupId) {
        MentorshipGroup group = announcementAccessService.requireMember(actorUserId, groupId);
        List<Announcement> visible = announcementRepository.findByGroupIdOrderByCreatedAtDesc(group.getId()).stream()
                .filter(item -> item.isVisibleTo(actorUserId))
                .toList();
        List<UUID> ids = visible.stream().map(Announcement::getId).toList();
        Map<UUID, Long> likeCounts = announcementLikeRepository.countByAnnouncementIds(ids);
        Set<UUID> likedIds = announcementLikeRepository.findLikedAnnouncementIds(actorUserId, ids);
        Map<UUID, Long> commentCounts = announcementCommentRepository.countActiveByAnnouncementIds(ids);
        return announcementMapper.toResponses(visible, actorUserId, likeCounts, likedIds, commentCounts);
    }
}
