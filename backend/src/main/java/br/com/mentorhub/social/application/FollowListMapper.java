package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.social.api.dto.FollowListResponse;
import br.com.mentorhub.social.api.dto.FollowUserResponse;
import br.com.mentorhub.social.domain.UserFollow;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

final class FollowListMapper {

    private FollowListMapper() {
    }

    static FollowListResponse map(
            Page<UserFollow> follows,
            Function<UserFollow, UUID> userIdExtractor,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        List<UUID> userIds = follows.getContent().stream().map(userIdExtractor).distinct().toList();
        Map<UUID, User> usersById = userRepository.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<UUID, UUID> mentorProfileIdByUserId = mentorProfileRepository.findByUserIdIn(userIds).stream()
                .filter(MentorProfile::isActive)
                .collect(Collectors.toMap(MentorProfile::getUserId, MentorProfile::getId, (left, right) -> left));

        List<FollowUserResponse> items = follows.getContent().stream()
                .map(follow -> {
                    UUID relatedUserId = userIdExtractor.apply(follow);
                    User user = usersById.get(relatedUserId);
                    return new FollowUserResponse(
                            relatedUserId,
                            user != null ? user.getName() : "Usuário",
                            user != null ? user.getRole().name() : null,
                            follow.getCreatedAt(),
                            mentorProfileIdByUserId.get(relatedUserId)
                    );
                })
                .toList();

        return new FollowListResponse(
                items,
                follows.getNumber(),
                follows.getSize(),
                follows.getTotalElements(),
                follows.getTotalPages(),
                follows.isLast()
        );
    }
}
