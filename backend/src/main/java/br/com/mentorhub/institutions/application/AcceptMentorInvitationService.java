package br.com.mentorhub.institutions.application;

import br.com.mentorhub.identity.api.dto.AuthTokenResponse;
import br.com.mentorhub.identity.api.dto.UserResponse;
import br.com.mentorhub.identity.domain.PasswordPolicy;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.institutions.api.dto.AcceptMentorInvitationRequest;
import br.com.mentorhub.institutions.domain.MentorInvitation;
import br.com.mentorhub.institutions.domain.MentorInvitationRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.exception.ConflictException;
import br.com.mentorhub.shared.exception.NotFoundException;
import br.com.mentorhub.shared.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AcceptMentorInvitationService {

    private final MentorInvitationRepository mentorInvitationRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AcceptMentorInvitationService(
            MentorInvitationRepository mentorInvitationRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.mentorInvitationRepository = mentorInvitationRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse execute(String token, AcceptMentorInvitationRequest request) {
        PasswordPolicy.validate(request.password());
        PasswordPolicy.requireConfirmation(request.password(), request.confirmPassword());
        if (request.confirmPassword() == null || request.confirmPassword().isBlank()) {
            throw new BusinessException("PASSWORD_MISMATCH", "As senhas não coincidem");
        }

        MentorInvitation invitation = mentorInvitationRepository.findByToken(token == null ? "" : token.trim())
                .orElseThrow(() -> new NotFoundException("Convite não encontrado"));
        invitation.assertAcceptable(invitation.getEmail());

        if (userRepository.existsByEmail(invitation.getEmail())) {
            throw new ConflictException("Este e-mail já possui conta");
        }

        User user = userRepository.save(User.register(
                invitation.getName(),
                invitation.getEmail(),
                passwordEncoder.encode(request.password()),
                UserRole.MENTOR
        ));

        MentorProfile profile = mentorProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> MentorProfile.create(user.getId()));
        profile.attachToInstitution(
                invitation.getInstitutionId(),
                invitation.getSpecialty() != null ? invitation.getSpecialty() : invitation.getProgram()
        );
        mentorProfileRepository.save(profile);

        invitation.accept(user.getId());
        mentorInvitationRepository.save(invitation);

        return new AuthTokenResponse(
                jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name()),
                "Bearer",
                jwtService.getExpirationMs(),
                UserResponse.from(user)
        );
    }
}
