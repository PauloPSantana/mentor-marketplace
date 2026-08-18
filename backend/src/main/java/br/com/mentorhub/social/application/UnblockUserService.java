package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.BlockStatusResponse;
import br.com.mentorhub.social.domain.UserBlockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnblockUserService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;

    public UnblockUserService(UserRepository userRepository, UserBlockRepository userBlockRepository) {
        this.userRepository = userRepository;
        this.userBlockRepository = userBlockRepository;
    }

    @Transactional
    public BlockStatusResponse execute(UUID blockerId, UUID blockedId) {
        userRepository.findById(blockedId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        userBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);

        return new BlockStatusResponse(
                false,
                userBlockRepository.existsByBlockerIdAndBlockedId(blockedId, blockerId)
        );
    }
}
