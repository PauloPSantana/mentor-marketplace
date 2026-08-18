package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.BlockStatusResponse;
import br.com.mentorhub.social.domain.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetBlockStatusService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;

    public GetBlockStatusService(UserRepository userRepository, UserBlockRepository userBlockRepository) {
        this.userRepository = userRepository;
        this.userBlockRepository = userBlockRepository;
    }

    @Transactional(readOnly = true)
    public BlockStatusResponse execute(UUID viewerId, UUID targetUserId) {
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return new BlockStatusResponse(
                userBlockRepository.existsByBlockerIdAndBlockedId(viewerId, targetUserId),
                userBlockRepository.existsByBlockerIdAndBlockedId(targetUserId, viewerId)
        );
    }
}
