package br.com.mentorhub.social.application;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.social.api.dto.BlockStatusResponse;
import br.com.mentorhub.social.domain.UserBlock;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BlockUserService {

    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserFollowRepository userFollowRepository;

    public BlockUserService(
            UserRepository userRepository,
            UserBlockRepository userBlockRepository,
            UserFollowRepository userFollowRepository
    ) {
        this.userRepository = userRepository;
        this.userBlockRepository = userBlockRepository;
        this.userFollowRepository = userFollowRepository;
    }

    @Transactional
    public BlockStatusResponse execute(UUID blockerId, UUID blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new BusinessException("SELF_BLOCK", "Usuário não pode bloquear a si mesmo");
        }

        userRepository.findById(blockerId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.findById(blockedId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (userBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw new BusinessException("ALREADY_BLOCKED", "Usuário já bloqueou este perfil");
        }

        userBlockRepository.save(UserBlock.create(blockerId, blockedId));
        userFollowRepository.deleteByFollowerIdAndFollowedId(blockerId, blockedId);
        userFollowRepository.deleteByFollowerIdAndFollowedId(blockedId, blockerId);

        return new BlockStatusResponse(true, false);
    }
}
