package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.PasswordPolicy;
import br.com.mentorhub.identity.domain.LinkedInProfileParser;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.ConflictException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User execute(String name, String email, String password, UserRole role) {
        return execute(name, email, password, null, role, null, null);
    }

    @Transactional
    public User execute(
            String name,
            String email,
            String password,
            String confirmPassword,
            UserRole role,
            String linkedinUrl,
            String photoUrl
    ) {
        PasswordPolicy.validate(password);
        PasswordPolicy.requireConfirmation(password, confirmPassword);
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email já cadastrado");
        }
        String normalizedLinkedIn = LinkedInProfileParser.normalizeOrNull(linkedinUrl);

        User user = User.register(name, normalizedEmail, passwordEncoder.encode(password), role);
        if (photoUrl != null && !photoUrl.isBlank()) {
            user.updatePhoto(photoUrl.trim());
        }
        User saved = userRepository.save(user);

        if (saved.getRole() == UserRole.MENTOR) {
            eventPublisher.publishEvent(new MentorUserRegisteredEvent(saved.getId(), normalizedLinkedIn, photoUrl));
        }

        return saved;
    }
}
