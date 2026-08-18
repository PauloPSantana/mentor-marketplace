package br.com.mentorhub.social.application;

import br.com.mentorhub.social.api.dto.FollowStatusResponse;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;

import java.util.UUID;

final class FollowStatusFactory {

    private FollowStatusFactory() {
    }

    static FollowStatusResponse build(
            UserFollowRepository userFollowRepository,
            UserBlockRepository userBlockRepository,
            UUID viewerId,
            UUID targetUserId
    ) {
        return new FollowStatusResponse(
                userFollowRepository.existsByFollowerIdAndFollowedId(viewerId, targetUserId),
                userFollowRepository.countByFollowedId(targetUserId),
                userFollowRepository.countByFollowerId(targetUserId),
                userBlockRepository.existsByBlockerIdAndBlockedId(viewerId, targetUserId),
                userBlockRepository.existsByBlockerIdAndBlockedId(targetUserId, viewerId)
        );
    }
}
