package br.com.mentorhub.identity.infrastructure.security;

import br.com.mentorhub.identity.domain.UserRepository;
import br.com.mentorhub.shared.security.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DomainUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return resolveUser(username)
                .map(user -> new AuthenticatedUser(
                        user.getId(),
                        user.getEmail(),
                        user.getPasswordHash(),
                        user.getRole().name(),
                        user.isActive()
                ))
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    private Optional<br.com.mentorhub.identity.domain.User> resolveUser(String username) {
        try {
            return userRepository.findById(UUID.fromString(username));
        } catch (IllegalArgumentException ex) {
            return userRepository.findByEmail(username.trim().toLowerCase());
        }
    }
}
