package br.com.mentorhub.institutions.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataMentorInvitationRepository extends JpaRepository<MentorInvitationJpaEntity, UUID> {

    Optional<MentorInvitationJpaEntity> findByToken(String token);

    List<MentorInvitationJpaEntity> findByInstitutionIdOrderByCreatedAtDesc(UUID institutionId);

    boolean existsByInstitutionIdAndEmailAndStatusAndExpiresAtAfter(
            UUID institutionId,
            String email,
            String status,
            Instant expiresAt
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            delete from MentorInvitationJpaEntity i
            where i.institutionId = :institutionId
              and i.email = :email
              and i.status <> 'ACCEPTED'
            """)
    void deleteNonAcceptedByInstitutionIdAndEmail(
            @Param("institutionId") UUID institutionId,
            @Param("email") String email
    );
}
