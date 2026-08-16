package br.com.mentorhub.identity.application;

import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.identity.domain.UserRole;
import br.com.mentorhub.shared.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private RegisterUserService service;

    @BeforeEach
    void setUp() {
        service = new RegisterUserService(userRepository, passwordEncoder, eventPublisher);
    }

    @Test
    void shouldRegisterNewUser() {
        when(userRepository.existsByEmail("mentor@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.execute("Mentor", "mentor@email.com", "senha12345", UserRole.MENTOR);

        assertEquals("Mentor", user.getName());
        assertEquals(UserRole.MENTOR, user.getRole());
        verify(userRepository).save(any(User.class));

        ArgumentCaptor<MentorUserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(MentorUserRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(user.getId(), eventCaptor.getValue().userId());
    }

    @Test
    void shouldNotPublishMentorEventForMentee() {
        when(userRepository.existsByEmail("mentee@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha12345")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.execute("Mentee", "mentee@email.com", "senha12345", UserRole.MENTEE);

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        when(userRepository.existsByEmail("mentor@email.com")).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> service.execute("Mentor", "mentor@email.com", "senha12345", UserRole.MENTOR)
        );
    }
}
