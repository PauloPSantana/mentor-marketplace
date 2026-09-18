package br.com.mentorhub.scheduling.domain;

import java.util.Optional;
import java.util.UUID;

public interface GoogleConnectionRepository {

    GoogleConnection save(GoogleConnection connection);

    Optional<GoogleConnection> findByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
