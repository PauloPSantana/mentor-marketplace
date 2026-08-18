package br.com.mentorhub.mentorships.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorshipRepository {

    Mentorship save(Mentorship mentorship);

    Optional<Mentorship> findById(UUID id);

    Optional<Mentorship> findByEnrollmentId(UUID enrollmentId);

    List<Mentorship> findByEnrollmentIdIn(Collection<UUID> enrollmentIds);

    boolean existsByEnrollmentId(UUID enrollmentId);

    Page<Mentorship> findByMentorUserId(UUID mentorUserId, MentorshipStatus status, Pageable pageable);

    Page<Mentorship> findByMenteeUserId(UUID menteeUserId, MentorshipStatus status, Pageable pageable);

    List<Mentorship> findByMentorUserId(UUID mentorUserId);

    List<Mentorship> findByMenteeUserId(UUID menteeUserId);
}
