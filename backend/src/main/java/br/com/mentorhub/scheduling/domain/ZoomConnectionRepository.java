package br.com.mentorhub.scheduling.domain;

import java.util.Optional;
import java.util.UUID;

public interface ZoomConnectionRepository {

    ZoomConnection save(ZoomConnection connection);

    Optional<ZoomConnection> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
