package br.com.mentorhub.enrollments.infrastructure.persistence;

import br.com.mentorhub.enrollments.domain.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataEnrollmentRepository extends JpaRepository<EnrollmentJpaEntity, UUID> {

    List<EnrollmentJpaEntity> findByMenteeUserIdOrderByCreatedAtDesc(UUID menteeUserId);

    List<EnrollmentJpaEntity> findByMentorshipIdInOrderByCreatedAtDesc(Collection<UUID> mentorshipIds);

    boolean existsByMentorshipIdAndMenteeUserIdAndStatusIn(
            UUID mentorshipId,
            UUID menteeUserId,
            Collection<EnrollmentStatus> statuses
    );

    long countByMentorshipIdAndStatusIn(UUID mentorshipId, Collection<EnrollmentStatus> statuses);

    Optional<EnrollmentJpaEntity> findFirstByMentorshipIdInAndMenteeUserIdOrderByCreatedAtDesc(
            Collection<UUID> mentorshipIds,
            UUID menteeUserId
    );
}
