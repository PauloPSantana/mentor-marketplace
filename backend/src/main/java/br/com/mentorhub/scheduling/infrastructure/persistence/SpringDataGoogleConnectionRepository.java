package br.com.mentorhub.scheduling.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataGoogleConnectionRepository extends JpaRepository<GoogleConnectionJpaEntity, UUID> {
}
