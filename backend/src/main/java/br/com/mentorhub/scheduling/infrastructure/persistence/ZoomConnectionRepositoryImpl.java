package br.com.mentorhub.scheduling.infrastructure.persistence;

import br.com.mentorhub.scheduling.domain.ZoomConnection;
import br.com.mentorhub.scheduling.domain.ZoomConnectionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ZoomConnectionRepositoryImpl implements ZoomConnectionRepository {

    private final SpringDataZoomConnectionRepository springDataZoomConnectionRepository;

    public ZoomConnectionRepositoryImpl(SpringDataZoomConnectionRepository springDataZoomConnectionRepository) {
        this.springDataZoomConnectionRepository = springDataZoomConnectionRepository;
    }

    @Override
    public ZoomConnection save(ZoomConnection connection) {
        return toDomain(springDataZoomConnectionRepository.save(toEntity(connection)));
    }

    @Override
    public Optional<ZoomConnection> findByUserId(UUID userId) {
        return springDataZoomConnectionRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataZoomConnectionRepository.deleteById(userId);
    }

    private ZoomConnectionJpaEntity toEntity(ZoomConnection connection) {
        ZoomConnectionJpaEntity entity = new ZoomConnectionJpaEntity();
        entity.setUserId(connection.getUserId());
        entity.setZoomUserId(connection.getZoomUserId());
        entity.setZoomEmail(connection.getZoomEmail());
        entity.setAccessToken(connection.getAccessToken());
        entity.setRefreshToken(connection.getRefreshToken());
        entity.setExpiresAt(connection.getExpiresAt());
        entity.setCreatedAt(connection.getCreatedAt());
        entity.setUpdatedAt(connection.getUpdatedAt());
        return entity;
    }

    private ZoomConnection toDomain(ZoomConnectionJpaEntity entity) {
        return ZoomConnection.restore(
                entity.getUserId(),
                entity.getZoomUserId(),
                entity.getZoomEmail(),
                entity.getAccessToken(),
                entity.getRefreshToken(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
