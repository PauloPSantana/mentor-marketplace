package br.com.mentorhub.institutions.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MentorInvitationRepository {

    MentorInvitation save(MentorInvitation invitation);

    Optional<MentorInvitation> findById(UUID id);

    Optional<MentorInvitation> findByToken(String token);

    List<MentorInvitation> findByInstitutionId(UUID institutionId);

    boolean existsPendingByInstitutionIdAndEmail(UUID institutionId, String email);

    void delete(MentorInvitation invitation);

    void deleteNonAcceptedByInstitutionIdAndEmail(UUID institutionId, String email);
}
