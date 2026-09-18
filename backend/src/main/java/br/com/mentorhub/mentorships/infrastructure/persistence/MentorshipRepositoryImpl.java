package br.com.mentorhub.mentorships.infrastructure.persistence;

import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorshipRepositoryImpl implements MentorshipRepository {

    private final SpringDataMentorshipRepository springDataMentorshipRepository;

    public MentorshipRepositoryImpl(SpringDataMentorshipRepository springDataMentorshipRepository) {
        this.springDataMentorshipRepository = springDataMentorshipRepository;
    }

    @Override
    public Mentorship save(Mentorship mentorship) {
        return toDomain(springDataMentorshipRepository.save(toEntity(mentorship)));
    }

    @Override
    public Optional<Mentorship> findById(UUID id) {
        return springDataMentorshipRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Mentorship> findByEnrollmentId(UUID enrollmentId) {
        return springDataMentorshipRepository.findByEnrollmentId(enrollmentId).map(this::toDomain);
    }

    @Override
    public List<Mentorship> findByEnrollmentIdIn(Collection<UUID> enrollmentIds) {
        if (enrollmentIds == null || enrollmentIds.isEmpty()) {
            return List.of();
        }
        return springDataMentorshipRepository.findByEnrollmentIdIn(enrollmentIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByEnrollmentId(UUID enrollmentId) {
        return springDataMentorshipRepository.existsByEnrollmentId(enrollmentId);
    }

    @Override
    public Page<Mentorship> findByMentorUserId(UUID mentorUserId, MentorshipStatus status, Pageable pageable) {
        Page<MentorshipJpaEntity> page = status == null
                ? springDataMentorshipRepository.findByMentorUserIdOrderByStartedAtDesc(mentorUserId, pageable)
                : springDataMentorshipRepository.findByMentorUserIdAndStatusOrderByStartedAtDesc(mentorUserId, status, pageable);
        return page.map(this::toDomain);
    }

    @Override
    public Page<Mentorship> findByMenteeUserId(UUID menteeUserId, MentorshipStatus status, Pageable pageable) {
        Page<MentorshipJpaEntity> page = status == null
                ? springDataMentorshipRepository.findByMenteeUserIdOrderByStartedAtDesc(menteeUserId, pageable)
                : springDataMentorshipRepository.findByMenteeUserIdAndStatusOrderByStartedAtDesc(menteeUserId, status, pageable);
        return page.map(this::toDomain);
    }

    @Override
    public List<Mentorship> findByMentorUserId(UUID mentorUserId) {
        return springDataMentorshipRepository.findByMentorUserId(mentorUserId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Mentorship> findByMenteeUserId(UUID menteeUserId) {
        return springDataMentorshipRepository.findByMenteeUserId(menteeUserId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Mentorship> findByMentorUserIdIn(Collection<UUID> mentorUserIds) {
        if (mentorUserIds == null || mentorUserIds.isEmpty()) {
            return List.of();
        }
        return springDataMentorshipRepository.findByMentorUserIdIn(mentorUserIds).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsOpenByMentorUserIdAndMenteeUserId(UUID mentorUserId, UUID menteeUserId) {
        return springDataMentorshipRepository.existsByMentorUserIdAndMenteeUserIdAndStatusIn(
                mentorUserId,
                menteeUserId,
                List.of(MentorshipStatus.PENDING, MentorshipStatus.ACTIVE, MentorshipStatus.PAUSED)
        );
    }

    @Override
    public Page<Mentorship> findByInstitutionId(UUID institutionId, MentorshipStatus status, Pageable pageable) {
        Page<MentorshipJpaEntity> page = status == null
                ? springDataMentorshipRepository.findByInstitutionIdOrderByStartedAtDesc(institutionId, pageable)
                : springDataMentorshipRepository.findByInstitutionIdAndStatusOrderByStartedAtDesc(institutionId, status, pageable);
        return page.map(this::toDomain);
    }

    @Override
    public List<Mentorship> findByInstitutionId(UUID institutionId) {
        if (institutionId == null) {
            return List.of();
        }
        return springDataMentorshipRepository.findByInstitutionId(institutionId).stream()
                .map(this::toDomain)
                .toList();
    }

    private MentorshipJpaEntity toEntity(Mentorship mentorship) {
        MentorshipJpaEntity entity = new MentorshipJpaEntity();
        entity.setId(mentorship.getId());
        entity.setEnrollmentId(mentorship.getEnrollmentId());
        entity.setMenteeUserId(mentorship.getMenteeUserId());
        entity.setMentorProfileId(mentorship.getMentorProfileId());
        entity.setMentorUserId(mentorship.getMentorUserId());
        entity.setProductId(mentorship.getProductId());
        entity.setInstitutionId(mentorship.getInstitutionId());
        entity.setProgram(mentorship.getProgram());
        entity.setStatus(mentorship.getStatus());
        entity.setStartedAt(mentorship.getStartedAt());
        entity.setPausedAt(mentorship.getPausedAt());
        entity.setCompletedAt(mentorship.getCompletedAt());
        entity.setCancelledAt(mentorship.getCancelledAt());
        entity.setStatusChangedByUserId(mentorship.getStatusChangedByUserId());
        entity.setCreatedAt(mentorship.getCreatedAt());
        entity.setUpdatedAt(mentorship.getUpdatedAt());
        return entity;
    }

    private Mentorship toDomain(MentorshipJpaEntity entity) {
        return Mentorship.restore(
                entity.getId(),
                entity.getEnrollmentId(),
                entity.getMenteeUserId(),
                entity.getMentorProfileId(),
                entity.getMentorUserId(),
                entity.getProductId(),
                entity.getInstitutionId(),
                entity.getProgram(),
                entity.getStatus(),
                entity.getStartedAt(),
                entity.getPausedAt(),
                entity.getCompletedAt(),
                entity.getCancelledAt(),
                entity.getStatusChangedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
