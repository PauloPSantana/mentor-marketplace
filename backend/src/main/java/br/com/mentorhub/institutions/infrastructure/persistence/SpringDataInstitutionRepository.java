package br.com.mentorhub.institutions.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataInstitutionRepository extends JpaRepository<InstitutionJpaEntity, UUID> {

    Optional<InstitutionJpaEntity> findByOwnerUserId(UUID ownerUserId);
}
