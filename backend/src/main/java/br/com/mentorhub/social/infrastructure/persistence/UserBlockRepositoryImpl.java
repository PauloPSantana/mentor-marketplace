package br.com.mentorhub.social.infrastructure.persistence;

import br.com.mentorhub.social.domain.UserBlock;
import br.com.mentorhub.social.domain.UserBlockRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Repository
public class UserBlockRepositoryImpl implements UserBlockRepository {

    private final SpringDataUserBlockRepository springDataUserBlockRepository;

    public UserBlockRepositoryImpl(SpringDataUserBlockRepository springDataUserBlockRepository) {
        this.springDataUserBlockRepository = springDataUserBlockRepository;
    }

    @Override
    public UserBlock save(UserBlock block) {
        return toDomain(springDataUserBlockRepository.save(toEntity(block)));
    }

    @Override
    public boolean existsByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId) {
        return springDataUserBlockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    public boolean existsEitherDirection(UUID userA, UUID userB) {
        return existsByBlockerIdAndBlockedId(userA, userB) || existsByBlockerIdAndBlockedId(userB, userA);
    }

    @Override
    @Transactional
    public void deleteByBlockerIdAndBlockedId(UUID blockerId, UUID blockedId) {
        springDataUserBlockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    @Override
    public Set<UUID> findRelatedUserIds(UUID userId) {
        return springDataUserBlockRepository.findRelatedUserIds(userId);
    }

    private UserBlockJpaEntity toEntity(UserBlock block) {
        UserBlockJpaEntity entity = new UserBlockJpaEntity();
        entity.setId(block.getId());
        entity.setBlockerId(block.getBlockerId());
        entity.setBlockedId(block.getBlockedId());
        entity.setCreatedAt(block.getCreatedAt());
        return entity;
    }

    private UserBlock toDomain(UserBlockJpaEntity entity) {
        return UserBlock.restore(
                entity.getId(),
                entity.getBlockerId(),
                entity.getBlockedId(),
                entity.getCreatedAt()
        );
    }
}
