package br.com.mentorhub.institutions.application;

import br.com.mentorhub.identity.application.MentorInvitationGate;
import br.com.mentorhub.identity.application.MentorUserRegisteredEvent;
import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Component
public class MentorInvitationAcceptanceService implements MentorInvitationGate {

    private final MentorInvitationRepository mentorInvitationRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public MentorInvitationAcceptanceService(
            MentorInvitationRepository mentorInvitationRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.mentorInvitationRepository = mentorInvitationRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @Override
    public void assertAcceptable(String token, String email) {
        loadPending(token).assertAcceptable(email);
    }

    @Override
    @Transactional
    public void accept(String token, UUID mentorUserId) {
        MentorInvitation invitation = loadPending(token);
        invitation.accept(mentorUserId);
        mentorInvitationRepository.save(invitation);
        MentorProfile profile = mentorProfileRepository.findByUserId(mentorUserId)
                .orElseGet(() -> MentorProfile.create(mentorUserId));
        profile.attachToInstitution(
                invitation.getInstitutionId(),
                invitation.getSpecialty() != null ? invitation.getSpecialty() : invitation.getProgram()
        );
        mentorProfileRepository.save(profile);
    }

    @Order
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(MentorUserRegisteredEvent event) {
        if (event.invitationToken() == null || event.invitationToken().isBlank()) {
            return;
        }
        accept(event.invitationToken(), event.userId());
    }

    private MentorInvitation loadPending(String token) {
        if (token == null || token.isBlank()) {
            throw new NotFoundException("Convite não encontrado");
        }
        return mentorInvitationRepository.findByToken(token.trim())
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
    }
}
