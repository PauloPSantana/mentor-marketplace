package br.com.mentorhub.mentors.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorProfileRepository {

    MentorProfile save(MentorProfile profile);

    Optional<MentorProfile> findById(UUID id);

    List<MentorProfile> findByIdIn(Collection<UUID> ids);

    Optional<MentorProfile> findByUserId(UUID userId);

    List<MentorProfile> findByUserIdIn(Collection<UUID> userIds);

    boolean existsByUserId(UUID userId);

    List<MentorProfile> findAllActive();
}
