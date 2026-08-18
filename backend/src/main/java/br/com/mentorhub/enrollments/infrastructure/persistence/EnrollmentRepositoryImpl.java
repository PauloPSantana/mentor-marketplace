package br.com.mentorhub.enrollments.infrastructure.persistence;

import br.com.mentorhub.enrollments.domain.Enrollment;
import br.com.mentorhub.enrollments.domain.EnrollmentRepository;
import br.com.mentorhub.enrollments.domain.EnrollmentStatus;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class EnrollmentRepositoryImpl implements EnrollmentRepository {

    private static final List<EnrollmentStatus> OPEN_STATUSES = List.of(
            EnrollmentStatus.PENDING,
            EnrollmentStatus.ACCEPTED
    );

    private final SpringDataEnrollmentRepository springDataEnrollmentRepository;

    public EnrollmentRepositoryImpl(SpringDataEnrollmentRepository springDataEnrollmentRepository) {
        this.springDataEnrollmentRepository = springDataEnrollmentRepository;
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        return toDomain(springDataEnrollmentRepository.save(toEntity(enrollment)));
    }

    @Override
    public Optional<Enrollment> findById(UUID id) {
        return springDataEnrollmentRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Enrollment> findByMenteeUserId(UUID menteeUserId) {
        return springDataEnrollmentRepository.findByMenteeUserIdOrderByCreatedAtDesc(menteeUserId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Enrollment> findByMentorshipIdIn(Collection<UUID> mentorshipIds) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            return Collections.emptyList();
        }
        return springDataEnrollmentRepository.findByMentorshipIdInOrderByCreatedAtDesc(mentorshipIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsOpenByMentorshipIdAndMenteeUserId(UUID mentorshipId, UUID menteeUserId) {
        return springDataEnrollmentRepository.existsByMentorshipIdAndMenteeUserIdAndStatusIn(
                mentorshipId,
                menteeUserId,
                OPEN_STATUSES
        );
    }

    @Override
    public long countOccupiedSeats(UUID mentorshipId) {
        return springDataEnrollmentRepository.countByMentorshipIdAndStatusIn(mentorshipId, OPEN_STATUSES);
    }

    @Override
    public Optional<Enrollment> findLatestByMentorshipIdsAndMenteeUserId(Collection<UUID> mentorshipIds, UUID menteeUserId) {
        if (mentorshipIds == null || mentorshipIds.isEmpty()) {
            return Optional.empty();
        }
        return springDataEnrollmentRepository
                .findFirstByMentorshipIdInAndMenteeUserIdOrderByCreatedAtDesc(mentorshipIds, menteeUserId)
                .map(this::toDomain);
    }

    private EnrollmentJpaEntity toEntity(Enrollment enrollment) {
        EnrollmentJpaEntity entity = new EnrollmentJpaEntity();
        entity.setId(enrollment.getId());
        entity.setMentorshipId(enrollment.getMentorshipId());
        entity.setMenteeUserId(enrollment.getMenteeUserId());
        entity.setStatus(enrollment.getStatus());
        entity.setPriceSnapshot(enrollment.getPriceSnapshot());
        entity.setPlatformFee(enrollment.getPlatformFee());
        entity.setMentorAmount(enrollment.getMentorAmount());
        entity.setMessage(enrollment.getMessage());
        entity.setCreatedAt(enrollment.getCreatedAt());
        entity.setUpdatedAt(enrollment.getUpdatedAt());
        entity.setRespondedAt(enrollment.getRespondedAt());
        entity.setCancelledAt(enrollment.getCancelledAt());
        return entity;
    }

    private Enrollment toDomain(EnrollmentJpaEntity entity) {
        return Enrollment.restore(
                entity.getId(),
                entity.getMentorshipId(),
                entity.getMenteeUserId(),
                entity.getStatus(),
                entity.getPriceSnapshot(),
                entity.getPlatformFee(),
                entity.getMentorAmount(),
                entity.getMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRespondedAt(),
                entity.getCancelledAt()
        );
    }
}
