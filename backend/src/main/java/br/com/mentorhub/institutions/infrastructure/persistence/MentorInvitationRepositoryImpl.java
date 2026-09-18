package br.com.mentorhub.institutions.infrastructure.persistence;

import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationRepository;
import br.com.mentorhub.institutions.domain.MentorInvitationStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MentorInvitationRepositoryImpl implements MentorInvitationRepository {

    private final SpringDataMentorInvitationRepository springDataMentorInvitationRepository;

    public MentorInvitationRepositoryImpl(SpringDataMentorInvitationRepository springDataMentorInvitationRepository) {
        this.springDataMentorInvitationRepository = springDataMentorInvitationRepository;
    }

    @Override
    public MentorInvitation save(MentorInvitation invitation) {
        return toDomain(springDataMentorInvitationRepository.save(toEntity(invitation)));
    }

    @Override
    public Optional<MentorInvitation> findById(UUID id) {
        return springDataMentorInvitationRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<MentorInvitation> findByToken(String token) {
        return springDataMentorInvitationRepository.findByToken(token).map(this::toDomain);
    }

    @Override
    public List<MentorInvitation> findByInstitutionId(UUID institutionId) {
        return springDataMentorInvitationRepository.findByInstitutionIdOrderByCreatedAtDesc(institutionId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsPendingByInstitutionIdAndEmail(UUID institutionId, String email) {
        return springDataMentorInvitationRepository.existsByInstitutionIdAndEmailAndStatusAndExpiresAtAfter(
                institutionId,
                email,
                MentorInvitationStatus.PENDING.name(),
                java.time.Instant.now()
        );
    }

    @Override
    public void delete(MentorInvitation invitation) {
        springDataMentorInvitationRepository.deleteById(invitation.getId());
    }

    @Override
    public void deleteNonAcceptedByInstitutionIdAndEmail(UUID institutionId, String email) {
        springDataMentorInvitationRepository.deleteNonAcceptedByInstitutionIdAndEmail(institutionId, email);
    }

    private MentorInvitationJpaEntity toEntity(MentorInvitation invitation) {
        MentorInvitationJpaEntity entity = new MentorInvitationJpaEntity();
        entity.setId(invitation.getId());
        entity.setInstitutionId(invitation.getInstitutionId());
        entity.setInvitedByUserId(invitation.getInvitedByUserId());
        entity.setName(invitation.getName());
        entity.setEmail(invitation.getEmail());
        entity.setSpecialty(invitation.getSpecialty());
        entity.setProgram(invitation.getProgram());
        entity.setToken(invitation.getToken());
        entity.setStatus(invitation.getStatus().name());
        entity.setExpiresAt(invitation.getExpiresAt());
        entity.setAcceptedUserId(invitation.getAcceptedUserId());
        entity.setAcceptedAt(invitation.getAcceptedAt());
        entity.setCreatedAt(invitation.getCreatedAt());
        entity.setUpdatedAt(invitation.getUpdatedAt());
        return entity;
    }

    private MentorInvitation toDomain(MentorInvitationJpaEntity entity) {
        return MentorInvitation.restore(
                entity.getId(),
                entity.getInstitutionId(),
                entity.getInvitedByUserId(),
                entity.getName(),
                entity.getEmail(),
                entity.getSpecialty(),
                entity.getProgram(),
                entity.getToken(),
                MentorInvitationStatus.valueOf(entity.getStatus()),
                entity.getExpiresAt(),
                entity.getAcceptedUserId(),
                entity.getAcceptedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
