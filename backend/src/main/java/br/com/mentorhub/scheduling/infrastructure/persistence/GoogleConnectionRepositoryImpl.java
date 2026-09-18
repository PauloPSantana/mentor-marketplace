package br.com.mentorhub.scheduling.infrastructure.persistence;

import br.com.mentorhub.scheduling.domain.GoogleConnection;
import br.com.mentorhub.scheduling.domain.GoogleConnectionRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class GoogleConnectionRepositoryImpl implements GoogleConnectionRepository {

    private final SpringDataGoogleConnectionRepository springDataGoogleConnectionRepository;

    public GoogleConnectionRepositoryImpl(SpringDataGoogleConnectionRepository springDataGoogleConnectionRepository) {
        this.springDataGoogleConnectionRepository = springDataGoogleConnectionRepository;
    }

    @Override
    public GoogleConnection save(GoogleConnection connection) {
        return toDomain(springDataGoogleConnectionRepository.save(toEntity(connection)));
    }

    @Override
    public Optional<GoogleConnection> findByUserId(UUID userId) {
        return springDataGoogleConnectionRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        springDataGoogleConnectionRepository.deleteById(userId);
    }

    private GoogleConnectionJpaEntity toEntity(GoogleConnection connection) {
        GoogleConnectionJpaEntity entity = new GoogleConnectionJpaEntity();
        entity.setUserId(connection.getUserId());
        entity.setGoogleUserId(connection.getGoogleUserId());
        entity.setGoogleEmail(connection.getGoogleEmail());
        entity.setAccessToken(connection.getAccessToken());
        entity.setRefreshToken(connection.getRefreshToken());
        entity.setExpiresAt(connection.getExpiresAt());
        entity.setCreatedAt(connection.getCreatedAt());
        entity.setUpdatedAt(connection.getUpdatedAt());
        return entity;
    }

    private GoogleConnection toDomain(GoogleConnectionJpaEntity entity) {
        return GoogleConnection.restore(
                entity.getUserId(),
                entity.getGoogleUserId(),
                entity.getGoogleEmail(),
                entity.getAccessToken(),
                entity.getRefreshToken(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
