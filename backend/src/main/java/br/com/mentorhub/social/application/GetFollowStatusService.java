package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.FollowStatusResponse;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetFollowStatusService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserBlockRepository userBlockRepository;

    public GetFollowStatusService(
            UserRepository userRepository,
            UserFollowRepository userFollowRepository,
            UserBlockRepository userBlockRepository
    ) {
        this.userRepository = userRepository;
        this.userFollowRepository = userFollowRepository;
        this.userBlockRepository = userBlockRepository;
    }

    @Transactional(readOnly = true)
    public FollowStatusResponse execute(UUID viewerId, UUID targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return FollowStatusFactory.build(userFollowRepository, userBlockRepository, viewerId, targetUserId);
    }
}
