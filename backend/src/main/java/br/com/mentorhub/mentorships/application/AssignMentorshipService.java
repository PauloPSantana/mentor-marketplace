package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.mentorships.api.dto.AssignMentorshipRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AssignMentorshipService {

    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final InstitutionRepository institutionRepository;
    private final MentorshipRepository mentorshipRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;

    public AssignMentorshipService(
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            InstitutionRepository institutionRepository,
            MentorshipRepository mentorshipRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper
    ) {
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.institutionRepository = institutionRepository;
        this.mentorshipRepository = mentorshipRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
    }

    @Transactional
    public MentorshipRelationshipResponse execute(UUID actorUserId, AssignMentorshipRequest request) {
        User actor = userRepository.findById(actorUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        MentorProfile mentorProfile = resolveMentorProfile(actor, request);
        User mentee = resolveMentee(request.menteeEmail());
        if (mentee.getId().equals(mentorProfile.getUserId())) {
            throw new BusinessException("INVALID_MENTORSHIP", "O mentor não pode ser vinculado a si mesmo");
        }
        if (mentorshipRepository.existsOpenByMentorUserIdAndMenteeUserId(mentorProfile.getUserId(), mentee.getId())) {
            throw new ConflictException("Já existe uma mentoria aberta entre este mentor e mentorado");
        }

        Mentorship saved = mentorshipRepository.save(Mentorship.assign(
                mentorProfile.getInstitutionId(),
                mentorProfile.getId(),
                mentorProfile.getUserId(),
                mentee.getId(),
                request.program(),
                actor.getId()
        ));
        return mentorshipRelationshipMapper.toResponse(saved, actor.getId());
    }

    private MentorProfile resolveMentorProfile(User actor, AssignMentorshipRequest request) {
        if (actor.getRole() == UserRole.MENTOR) {
            MentorProfile profile = mentorProfileRepository.findByUserId(actor.getId())
                    .orElseThrow(() -> new NotFoundException("Perfil de mentor não encontrado"));
            if (!profile.isActive()) {
                throw new BusinessException("INACTIVE_MENTOR", "Seu perfil de mentor está inativo");
            }
            return profile;
        }
        if (actor.getRole() == UserRole.INSTITUTION) {
            if (request.mentorProfileId() == null) {
                throw new BusinessException("MENTOR_REQUIRED", "Selecione o mentor para o vínculo");
            }
            Institution institution = institutionRepository.findByOwnerUserId(actor.getId())
                    .orElseThrow(() -> new NotFoundException("Instituição não encontrada"));
            MentorProfile profile = mentorProfileRepository.findById(request.mentorProfileId())
                    .orElseThrow(() -> new NotFoundException("Mentor não encontrado"));
            if (!institution.getId().equals(profile.getInstitutionId())) {
                throw new AccessDeniedException("Este mentor não pertence à sua instituição");
            }
            if (!profile.isActive()) {
                throw new BusinessException("INACTIVE_MENTOR", "Este mentor está inativo");
            }
            return profile;
        }
        throw new AccessDeniedException("Somente mentor ou instituição podem vincular mentorados");
    }

    private User resolveMentee(String rawEmail) {
        String email = rawEmail == null ? "" : rawEmail.trim().toLowerCase();
        User mentee = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        "Mentorado não encontrado. O mentorado precisa ter uma conta na plataforma."
                ));
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new BusinessException("INVALID_MENTEE", "O e-mail informado não pertence a um mentorado");
        }
        if (!mentee.isActive()) {
            throw new BusinessException("INACTIVE_MENTEE", "Este mentorado está inativo");
        }
        return mentee;
    }
}
