package br.com.mentorhub.announcements.application;

import br.com.mentorhub.announcements.api.dto.AnnouncementRecipientResponse;
import br.com.mentorhub.announcements.api.dto.AnnouncementResponse;
import br.com.mentorhub.announcements.domain.Announcement;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class AnnouncementMapper {

    private final UserRepository userRepository;

    public AnnouncementMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<AnnouncementResponse> toResponses(
            Collection<Announcement> announcements,
            UUID viewerUserId,
            Map<UUID, Long> likeCounts,
            Set<UUID> likedIds,
            Map<UUID, Long> commentCounts
    ) {
        if (announcements == null || announcements.isEmpty()) {
            return List.of();
        }
        List<UUID> userIds = announcements.stream()
                .flatMap(item -> Stream.concat(Stream.of(item.getAuthorUserId()), item.getRecipientUserIds().stream()))
                .distinct()
                .toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return announcements.stream()
                .map(item -> toResponse(
                        item,
                        usersById,
                        likeCounts.getOrDefault(item.getId(), 0L),
                        likedIds.contains(item.getId()),
                        commentCounts.getOrDefault(item.getId(), 0L)
                ))
                .toList();
    }

    public AnnouncementResponse toResponse(
            Announcement announcement,
            long likeCount,
            boolean liked,
            long commentCount
    ) {
        return toResponses(List.of(announcement), announcement.getAuthorUserId(), Map.of(announcement.getId(), likeCount),
                liked ? Set.of(announcement.getId()) : Set.of(),
                Map.of(announcement.getId(), commentCount)
        ).get(0);
    }

    private AnnouncementResponse toResponse(
            Announcement announcement,
            Map<UUID, User> usersById,
            long likeCount,
            boolean liked,
            long commentCount
    ) {
        User author = usersById.get(announcement.getAuthorUserId());
        List<AnnouncementRecipientResponse> recipients = announcement.getRecipientUserIds().stream()
                .map(userId -> {
                    User user = usersById.get(userId);
                    return new AnnouncementRecipientResponse(userId, user != null ? user.getName() : "Participante");
                })
                .toList();
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getGroupId(),
                announcement.getTitle(),
                announcement.getBody(),
                announcement.getAudienceType(),
                announcement.getAuthorUserId(),
                author != null ? author.getName() : "Mentor",
                author != null ? author.getPhotoUrl() : null,
                author != null ? author.getRole().name() : "MENTOR",
                recipients,
                likeCount,
                liked,
                commentCount,
                announcement.getCreatedAt()
        );
    }
}
