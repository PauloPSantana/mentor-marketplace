package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMentorshipSessionRepository extends JpaRepository<MentorshipSessionJpaEntity, UUID> {

    List<MentorshipSessionJpaEntity> findByMentorshipIdOrderByScheduledAtAsc(UUID mentorshipId);

    List<MentorshipSessionJpaEntity> findByMentorshipIdInAndStatus(
            Collection<UUID> mentorshipIds,
            MentorshipSessionStatus status
    );

    List<MentorshipSessionJpaEntity> findByMentorshipIdInAndScheduledAtBetweenOrderByScheduledAtAsc(
            Collection<UUID> mentorshipIds,
            Instant from,
            Instant to
    );

    Optional<MentorshipSessionJpaEntity> findFirstByMentorshipIdAndStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
            UUID mentorshipId,
            MentorshipSessionStatus status,
            Instant from
    );

    List<MentorshipSessionJpaEntity> findByMentorshipIdInAndStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
            Collection<UUID> mentorshipIds,
            MentorshipSessionStatus status,
            Instant from
    );

    long countByMentorshipIdAndStatusIn(UUID mentorshipId, Collection<MentorshipSessionStatus> statuses);

    List<MentorshipSessionJpaEntity> findByStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
            MentorshipSessionStatus status,
            Instant from
    );

    List<MentorshipSessionJpaEntity> findByMentorshipIdIn(Collection<UUID> mentorshipIds);

    Optional<MentorshipSessionJpaEntity> findByZoomMeetingId(String zoomMeetingId);
}
