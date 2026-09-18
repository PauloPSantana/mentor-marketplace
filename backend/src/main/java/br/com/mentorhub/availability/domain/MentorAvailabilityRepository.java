package br.com.mentorhub.availability.domain;

import java.util.Optional;
import java.util.UUID;

public interface MentorAvailabilityRepository {

    MentorAvailability save(MentorAvailability availability);

    Optional<MentorAvailability> findByMentorProfileId(UUID mentorProfileId);
}
