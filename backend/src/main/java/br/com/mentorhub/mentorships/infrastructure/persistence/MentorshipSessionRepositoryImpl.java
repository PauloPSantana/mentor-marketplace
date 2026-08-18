package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.MentorshipSession;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorshipSessionRepositoryImpl implements MentorshipSessionRepository {

    private final SpringDataMentorshipSessionRepository springDataMentorshipSessionRepository;

    public MentorshipSessionRepositoryImpl(SpringDataMentorshipSessionRepository springDataMentorshipSessionRepository) {
        this.springDataMentorshipSessionRepository = springDataMentorshipSessionRepository;
    }

    @Override
    public MentorshipSession save(MentorshipSession session) {
        return toDomain(springDataMentorshipSessionRepository.save(toEntity(session)));
    }

    @Override
    public Optional<MentorshipSession> findById(UUID id) {
        return springDataMentorshipSessionRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<MentorshipSession> findByMentorshipIdOrderByScheduledAtAsc(UUID mentorshipId) {
        return springDataMentorshipSessionRepository.findByMentorshipIdOrderByScheduledAtAsc(mentorshipId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MentorshipSession> findByMentorshipIdInAndStatus(
            Collection<UUID> mentorshipIds,
            MentorshipSessionStatus status
    ) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            return List.of();
        }
        return springDataMentorshipSessionRepository.findByMentorshipIdInAndStatus(mentorshipIds, status).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<MentorshipSession> findByMentorshipIdInAndScheduledAtBetweenOrderByScheduledAtAsc(
            Collection<UUID> mentorshipIds,
            Instant from,
            Instant to
    ) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            return List.of();
        }
        return springDataMentorshipSessionRepository
                .findByMentorshipIdInAndScheduledAtBetweenOrderByScheduledAtAsc(mentorshipIds, from, to)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<MentorshipSession> findNextScheduled(UUID mentorshipId, Instant from) {
        return springDataMentorshipSessionRepository
                .findFirstByMentorshipIdAndStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
                        mentorshipId,
                        MentorshipSessionStatus.SCHEDULED,
                        from
                )
                .map(this::toDomain);
    }

    @Override
    public List<MentorshipSession> findNextScheduledByMentorshipIdIn(Collection<UUID> mentorshipIds, Instant from) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            return List.of();
        }
        return springDataMentorshipSessionRepository
                .findByMentorshipIdInAndStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(
                        mentorshipIds,
                        MentorshipSessionStatus.SCHEDULED,
                        from
                )
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countByMentorshipIdAndStatusIn(UUID mentorshipId, Collection<MentorshipSessionStatus> statuses) {
        return springDataMentorshipSessionRepository.countByMentorshipIdAndStatusIn(mentorshipId, statuses);
    }

    @Override
    public List<MentorshipSession> findScheduledForReminders(Instant from) {
        return springDataMentorshipSessionRepository
                .findByStatusAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(MentorshipSessionStatus.SCHEDULED, from)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private MentorshipSessionJpaEntity toEntity(MentorshipSession session) {
        MentorshipSessionJpaEntity entity = new MentorshipSessionJpaEntity();
        entity.setId(session.getId());
        entity.setMentorshipId(session.getMentorshipId());
        entity.setScheduledAt(session.getScheduledAt());
        entity.setDurationMinutes(session.getDurationMinutes());
        entity.setMeetingUrl(session.getMeetingUrl());
        entity.setStatus(session.getStatus());
        entity.setNotes(session.getNotes());
        entity.setCreatedByUserId(session.getCreatedByUserId());
        entity.setCompletedAt(session.getCompletedAt());
        entity.setCompletedByUserId(session.getCompletedByUserId());
        entity.setCancelledAt(session.getCancelledAt());
        entity.setCancelledByUserId(session.getCancelledByUserId());
        entity.setCancelReason(session.getCancelReason());
        entity.setReminder24hSentAt(session.getReminder24hSentAt());
        entity.setReminder1hSentAt(session.getReminder1hSentAt());
        entity.setCreatedAt(session.getCreatedAt());
        entity.setUpdatedAt(session.getUpdatedAt());
        return entity;
    }

    private MentorshipSession toDomain(MentorshipSessionJpaEntity entity) {
        return MentorshipSession.restore(
                entity.getId(),
                entity.getMentorshipId(),
                entity.getScheduledAt(),
                entity.getDurationMinutes(),
                entity.getMeetingUrl(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedByUserId(),
                entity.getCompletedAt(),
                entity.getCompletedByUserId(),
                entity.getCancelledAt(),
                entity.getCancelledByUserId(),
                entity.getCancelReason(),
                entity.getReminder24hSentAt(),
                entity.getReminder1hSentAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
