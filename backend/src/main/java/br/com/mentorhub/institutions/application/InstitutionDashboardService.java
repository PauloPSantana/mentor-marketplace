package br.com.mentorhub.institutions.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.api.dto.InstitutionDashboardResponse;
import br.com.mentorhub.institutions.api.dto.InstitutionDashboardResponse.InstitutionInvitationResponse;
import br.com.mentorhub.institutions.api.dto.InstitutionDashboardResponse.InstitutionMentorResponse;
import br.com.mentorhub.institutions.api.dto.InviteMentorRequest;
import br.com.mentorhub.institutions.api.dto.MailStatusResponse;
import br.com.mentorhub.institutions.api.dto.PublicMentorInvitationResponse;
import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionRepository;
import br.com.mentorhub.mentorships.domain.MentorshipSessionStatus;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.shared.net.LanAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InstitutionDashboardService {

    private static final Logger log = LoggerFactory.getLogger(InstitutionDashboardService.class);

    private final InstitutionRepository institutionRepository;
    private final MentorInvitationRepository mentorInvitationRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipSessionRepository mentorshipSessionRepository;
    private final UserRepository userRepository;
    private final MentorInvitationMailer mentorInvitationMailer;
    private final String frontendBaseUrl;

    public InstitutionDashboardService(
            InstitutionRepository institutionRepository,
            MentorInvitationRepository mentorInvitationRepository,
            MentorProfileRepository mentorProfileRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipSessionRepository mentorshipSessionRepository,
            UserRepository userRepository,
            MentorInvitationMailer mentorInvitationMailer,
            @Value("${mentorhub.frontend-base-url:http://localhost:3000}") String frontendBaseUrl
    ) {
        this.institutionRepository = institutionRepository;
        this.mentorInvitationRepository = mentorInvitationRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipSessionRepository = mentorshipSessionRepository;
        this.userRepository = userRepository;
        this.mentorInvitationMailer = mentorInvitationMailer;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
    }

    @Transactional(readOnly = true)
    public InstitutionDashboardResponse dashboard(UUID actorUserId) {
        Institution institution = requireInstitution(actorUserId);
        List<MentorProfile> mentors;
        try {
            mentors = mentorProfileRepository.findByInstitutionId(institution.getId());
        } catch (RuntimeException ex) {
            log.warn("Falha ao listar mentores da instituição {}", institution.getId(), ex);
            mentors = List.of();
        }
        List<UUID> mentorUserIds = mentors.stream().map(MentorProfile::getUserId).toList();
        Map<UUID, User> users = userRepository.findAllByIds(mentorUserIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        List<Mentorship> mentorships = mentorshipRepository.findByMentorUserIdIn(mentorUserIds);
        Map<UUID, List<Mentorship>> byMentor = mentorships.stream()
                .collect(Collectors.groupingBy(Mentorship::getMentorUserId));
        List<UUID> mentorshipIds = mentorships.stream().map(Mentorship::getId).toList();
        Map<UUID, Long> completedByMentorship = mentorshipIds.isEmpty()
                ? Map.of()
                : mentorshipSessionRepository
                .findByMentorshipIdInAndStatus(mentorshipIds, MentorshipSessionStatus.COMPLETED)
                .stream()
                .collect(Collectors.groupingBy(
                        session -> session.getMentorshipId(),
                        Collectors.counting()
                ));

        List<InstitutionMentorResponse> mentorList = mentors.stream()
                .map(profile -> {
                    List<Mentorship> owned = byMentor.getOrDefault(profile.getUserId(), List.of());
                    int menteeCount = (int) owned.stream().map(Mentorship::getMenteeUserId).distinct().count();
                    int sessionCount = owned.stream()
                            .mapToInt(item -> completedByMentorship.getOrDefault(item.getId(), 0L).intValue())
                            .sum();
                    User mentor = users.get(profile.getUserId());
                    return new InstitutionMentorResponse(
                            profile.getId(),
                            profile.getUserId(),
                            mentor == null ? "Mentor" : mentor.getName(),
                            profile.getHeadline(),
                            menteeCount,
                            sessionCount,
                            profile.isActive()
                    );
                })
                .toList();

        int mentees = (int) mentorships.stream().map(Mentorship::getMenteeUserId).distinct().count();
        int activeMentorships = (int) mentorships.stream().filter(item -> item.getStatus() == MentorshipStatus.ACTIVE).count();
        int completedSessions = completedByMentorship.values().stream().mapToInt(Long::intValue).sum();
        List<InstitutionInvitationResponse> invitations = mentorInvitationRepository.findByInstitutionId(institution.getId())
                .stream()
                .map(invitation -> InstitutionInvitationResponse.from(invitation, inviteUrl(invitation.getToken())))
                .toList();
        return InstitutionDashboardResponse.of(
                institution,
                mentors.size(),
                mentees,
                activeMentorships,
                completedSessions,
                mentorList,
                invitations
        );
    }

    @Transactional
    public InstitutionInvitationResponse invite(UUID actorUserId, InviteMentorRequest request) {
        Institution institution = requireInstitution(actorUserId);
        String email = request.email().trim().toLowerCase();
        if (mentorInvitationRepository.existsPendingByInstitutionIdAndEmail(institution.getId(), email)) {
            throw new ConflictException("Já existe um convite pendente para este e-mail");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Este e-mail já possui conta. Peça ao mentor para se vincular depois do cadastro");
        }
        mentorInvitationRepository.deleteNonAcceptedByInstitutionIdAndEmail(institution.getId(), email);
        MentorInvitation invitation = MentorInvitation.create(
                institution.getId(),
                actorUserId,
                request.name(),
                email,
                request.specialty(),
                request.program()
        );
        mentorInvitationRepository.save(invitation);
        boolean emailSent = trySendInvite(invitation, institution.getName());
        return InstitutionInvitationResponse.from(invitation, inviteUrl(invitation.getToken()), emailSent);
    }

    @Transactional
    public InstitutionInvitationResponse resend(UUID actorUserId, UUID invitationId) {
        Institution institution = requireInstitution(actorUserId);
        MentorInvitation invitation = requireOwnedInvitation(institution, invitationId);
        invitation.refreshForResend();
        mentorInvitationRepository.save(invitation);
        boolean emailSent = trySendInvite(invitation, institution.getName());
        return InstitutionInvitationResponse.from(invitation, inviteUrl(invitation.getToken()), emailSent);
    }

    @Transactional
    public void remove(UUID actorUserId, UUID invitationId) {
        Institution institution = requireInstitution(actorUserId);
        MentorInvitation invitation = requireOwnedInvitation(institution, invitationId);
        invitation.assertRemovable();
        mentorInvitationRepository.delete(invitation);
    }

    @Transactional
    public void cancel(UUID actorUserId, UUID invitationId) {
        Institution institution = requireInstitution(actorUserId);
        MentorInvitation invitation = requireOwnedInvitation(institution, invitationId);
        invitation.cancel();
        mentorInvitationRepository.save(invitation);
    }

    @Transactional
    public void deactivateMentor(UUID actorUserId, UUID mentorProfileId) {
        Institution institution = requireInstitution(actorUserId);
        MentorProfile profile = mentorProfileRepository.findById(mentorProfileId)
                .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
        if (!institution.getId().equals(profile.getInstitutionId())) {
            throw new AccessDeniedException("Este mentor não pertence à sua instituição");
        }
        profile.setActive(false);
        mentorProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public PublicMentorInvitationResponse publicInvitation(String token) {
        MentorInvitation invitation = mentorInvitationRepository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        invitation.assertAcceptable(invitation.getEmail());
        String institutionName = institutionRepository.findById(invitation.getInstitutionId())
                .map(Institution::getName)
                .orElse("Instituição");
        return PublicMentorInvitationResponse.from(invitation, institutionName);
    }

    @Transactional(readOnly = true)
    public MailStatusResponse mailStatus() {
        return new MailStatusResponse(mentorInvitationMailer.isEnabled());
    }

    private boolean trySendInvite(MentorInvitation invitation, String institutionName) {
        try {
            return mentorInvitationMailer.sendInvite(
                    invitation.getEmail(),
                    invitation.getName(),
                    institutionName,
                    inviteUrl(invitation.getToken())
            );
        } catch (BusinessException ex) {
            log.warn("Convite salvo, mas o e-mail não saiu. dest={} code={}", invitation.getEmail(), ex.getCode());
            return false;
        }
    }

    private Institution requireInstitution(UUID actorUserId) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        if (actor.getRole() != UserRole.INSTITUTION) {
            throw new AccessDeniedException("Somente a instituição pode acessar este painel");
        }
        return institutionRepository.findByOwnerUserId(actorUserId)
                .orElseThrow(() -> new BusinessException("INSTITUTION_MISSING", "Instituição não encontrada para este usuário"));
    }

    private MentorInvitation requireOwnedInvitation(Institution institution, UUID invitationId) {
        MentorInvitation invitation = mentorInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        if (!institution.getId().equals(invitation.getInstitutionId())) {
            throw new AccessDeniedException("Este convite não pertence à sua instituição");
        }
        return invitation;
    }

    private String inviteUrl(String token) {
        return LanAddress.replaceLocalhost(frontendBaseUrl) + "/convites/mentor/" + token;
    }
}
