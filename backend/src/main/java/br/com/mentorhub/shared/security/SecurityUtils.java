package br.com.mentorhub.shared.security;

import br.com.mentorhub.shared.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public static UUID requireCurrentUserId() {
        return currentUser()
                .map(AuthenticatedUser::getId)
                .orElseThrow(() -> new UnauthorizedException("Usuário não autenticado"));
    }
}
