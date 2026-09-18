package br.com.mentorhub.mentorships.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.domain.Institution;
import br.com.mentorhub.institutions.domain.InstitutionRepository;
import br.com.mentorhub.mentorships.api.dto.MentorshipPageResponse;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
import br.com.mentorhub.mentorships.domain.Mentorship;
import br.com.mentorhub.mentorships.domain.MentorshipRepository;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import br.com.mentorhub.shared.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListMentorshipsService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final MentorshipRepository mentorshipRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final MentorshipRelationshipMapper mentorshipRelationshipMapper;

    public ListMentorshipsService(
            MentorshipRepository mentorshipRepository,
            UserRepository userRepository,
            InstitutionRepository institutionRepository,
            MentorshipRelationshipMapper mentorshipRelationshipMapper
    ) {
        this.mentorshipRepository = mentorshipRepository;
        this.userRepository = userRepository;
        this.institutionRepository = institutionRepository;
        this.mentorshipRelationshipMapper = mentorshipRelationshipMapper;
    }

    @Transactional(readOnly = true)
    public MentorshipPageResponse asMentor(UUID currentUserId, MentorshipStatus status, int page, int size) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Page<Mentorship> mentorships = mentorshipRepository.findByMentorUserId(
                currentUserId,
                status,
                PageRequest.of(safePage(page), safeSize(size))
        );
        return toPage(mentorships, currentUserId);
    }

    @Transactional(readOnly = true)
    public MentorshipPageResponse asMentee(UUID currentUserId, MentorshipStatus status, int page, int size) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Page<Mentorship> mentorships = mentorshipRepository.findByMenteeUserId(
                currentUserId,
                status,
                PageRequest.of(safePage(page), safeSize(size))
        );
        return toPage(mentorships, currentUserId);
    }

    @Transactional(readOnly = true)
    public MentorshipPageResponse asInstitution(UUID currentUserId, MentorshipStatus status, int page, int size) {
        Institution institution = institutionRepository.findByOwnerUserId(currentUserId)
                .orElseThrow(() -> new NotFoundException("Instituição não encontrada"));
        Page<Mentorship> mentorships = mentorshipRepository.findByInstitutionId(
                institution.getId(),
                status,
                PageRequest.of(safePage(page), safeSize(size))
        );
        return toPage(mentorships, currentUserId);
    }

    @Transactional(readOnly = true)
    public MentorshipRelationshipResponse getById(UUID currentUserId, UUID mentorshipId) {
        User actor = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        Mentorship mentorship = mentorshipRepository.findById(mentorshipId)
                .orElseThrow(() -> new NotFoundException("Mentoria não encontrada"));
        if (!canView(actor, mentorship)) {
            throw new AccessDeniedException("Somente os participantes podem consultar esta mentoria");
        }
        return mentorshipRelationshipMapper.toResponse(mentorship, currentUserId);
    }

    private boolean canView(User actor, Mentorship mentorship) {
        if (mentorship.isParticipant(actor.getId()) || actor.getRole() == UserRole.ADMIN) {
            return true;
        }
        if (actor.getRole() != UserRole.INSTITUTION || mentorship.getInstitutionId() == null) {
            return false;
        }
        return institutionRepository.findByOwnerUserId(actor.getId())
                .map(institution -> institution.getId().equals(mentorship.getInstitutionId()))
                .orElse(false);
    }

    private MentorshipPageResponse toPage(Page<Mentorship> mentorships, UUID viewerUserId) {
        return new MentorshipPageResponse(
                mentorshipRelationshipMapper.toResponses(mentorships.getContent(), viewerUserId),
                mentorships.getNumber(),
                mentorships.getSize(),
                mentorships.getTotalElements(),
                mentorships.getTotalPages(),
                mentorships.isLast()
        );
    }

    private static int safePage(int page) {
        return Math.max(page, 0);
    }

    private static int safeSize(int size) {
        return size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
    }
}
