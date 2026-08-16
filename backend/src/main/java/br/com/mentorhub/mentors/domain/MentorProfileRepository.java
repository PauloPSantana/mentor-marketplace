package br.com.mentorhub.mentors.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorProfileRepository {

    MentorProfile save(MentorProfile profile);

    Optional<MentorProfile> findById(UUID id);

    Optional<MentorProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    List<MentorProfile> findAllActive();
}
