package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.exception.UnauthorizedException;
import br.com.mentorhub.shared.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResult execute(String email, String password) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        return new LoginResult(token, "Bearer", jwtService.getExpirationMs(), user);
    }

    public record LoginResult(String accessToken, String tokenType, long expiresInMs, User user) {
    }
}
