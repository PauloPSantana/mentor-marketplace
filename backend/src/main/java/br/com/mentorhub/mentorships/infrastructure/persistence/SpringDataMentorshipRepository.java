package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMentorshipRepository extends JpaRepository<MentorshipJpaEntity, UUID> {

    Optional<MentorshipJpaEntity> findByEnrollmentId(UUID enrollmentId);

    List<MentorshipJpaEntity> findByEnrollmentIdIn(Collection<UUID> enrollmentIds);

    boolean existsByEnrollmentId(UUID enrollmentId);

    Page<MentorshipJpaEntity> findByMentorUserIdOrderByStartedAtDesc(UUID mentorUserId, Pageable pageable);

    Page<MentorshipJpaEntity> findByMentorUserIdAndStatusOrderByStartedAtDesc(
            UUID mentorUserId,
            MentorshipStatus status,
            Pageable pageable
    );

    Page<MentorshipJpaEntity> findByMenteeUserIdOrderByStartedAtDesc(UUID menteeUserId, Pageable pageable);

    Page<MentorshipJpaEntity> findByMenteeUserIdAndStatusOrderByStartedAtDesc(
            UUID menteeUserId,
            MentorshipStatus status,
            Pageable pageable
    );

    List<MentorshipJpaEntity> findByMentorUserId(UUID mentorUserId);

    List<MentorshipJpaEntity> findByMenteeUserId(UUID menteeUserId);
}
