package br.com.mentorhub.institutions.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository {

    Institution save(Institution institution);

    Optional<Institution> findById(UUID id);

    Optional<Institution> findByOwnerUserId(UUID ownerUserId);
}
