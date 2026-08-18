package br.com.mentorhub.mentorships.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorshipSessionRepository {

    MentorshipSession save(MentorshipSession session);

    Optional<MentorshipSession> findById(UUID id);

    List<MentorshipSession> findByMentorshipIdOrderByScheduledAtAsc(UUID mentorshipId);

    List<MentorshipSession> findByMentorshipIdInAndStatus(
            Collection<UUID> mentorshipIds,
            MentorshipSessionStatus status
    );

    List<MentorshipSession> findByMentorshipIdInAndScheduledAtBetweenOrderByScheduledAtAsc(
            Collection<UUID> mentorshipIds,
            Instant from,
            Instant to
    );

    Optional<MentorshipSession> findNextScheduled(UUID mentorshipId, Instant from);

    List<MentorshipSession> findNextScheduledByMentorshipIdIn(Collection<UUID> mentorshipIds, Instant from);

    long countByMentorshipIdAndStatusIn(UUID mentorshipId, Collection<MentorshipSessionStatus> statuses);

    List<MentorshipSession> findScheduledForReminders(Instant from);
}
