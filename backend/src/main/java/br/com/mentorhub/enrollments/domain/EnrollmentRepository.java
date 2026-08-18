package br.com.mentorhub.enrollments.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository {

    Enrollment save(Enrollment enrollment);

    Optional<Enrollment> findById(UUID id);

    List<Enrollment> findByMenteeUserId(UUID menteeUserId);

    List<Enrollment> findByMentorshipIdIn(Collection<UUID> mentorshipIds);

    boolean existsOpenByMentorshipIdAndMenteeUserId(UUID mentorshipId, UUID menteeUserId);

    long countOccupiedSeats(UUID mentorshipId);

    Optional<Enrollment> findLatestByMentorshipIdsAndMenteeUserId(Collection<UUID> mentorshipIds, UUID menteeUserId);
}
