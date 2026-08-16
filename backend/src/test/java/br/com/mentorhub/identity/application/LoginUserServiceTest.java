package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.UnauthorizedException;
import br.com.mentorhub.shared.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private LoginUserService service;

    @BeforeEach
    void setUp() {
        service = new LoginUserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = User.register("Ana", "ana@email.com", "encoded", UserRole.MENTEE);
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha12345", "encoded")).thenReturn(true);
        when(jwtService.generateToken(eq(user.getId()), eq("ana@email.com"), eq("MENTEE"))).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        LoginUserService.LoginResult result = service.execute("ana@email.com", "senha12345");

        assertEquals("jwt-token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(user.getId(), result.user().getId());
    }

    @Test
    void shouldRejectInvalidPassword() {
        User user = User.register("Ana", "ana@email.com", "encoded", UserRole.MENTEE);
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), eq("encoded"))).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.execute("ana@email.com", "errada"));
    }
}
