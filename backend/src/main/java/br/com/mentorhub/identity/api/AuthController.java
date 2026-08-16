package br.com.mentorhub.identity.api;

import br.com.mentorhub.identity.api.dto.AuthTokenResponse;
import br.com.mentorhub.identity.api.dto.LoginRequest;
import br.com.mentorhub.identity.api.dto.RegisterRequest;
import br.com.mentorhub.identity.api.dto.UserResponse;
import br.com.mentorhub.identity.application.GetCurrentUserService;
import br.com.mentorhub.identity.application.LoginUserService;
import br.com.mentorhub.identity.application.RegisterUserService;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final GetCurrentUserService getCurrentUserService;

    public AuthController(
            RegisterUserService registerUserService,
            LoginUserService loginUserService,
            GetCurrentUserService getCurrentUserService
    ) {
        this.registerUserService = registerUserService;
        this.loginUserService = loginUserService;
        this.getCurrentUserService = getCurrentUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserService.execute(
                request.name(),
                request.email(),
                request.password(),
                request.role()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUserService.LoginResult result = loginUserService.execute(request.email(), request.password());
        return ResponseEntity.ok(new AuthTokenResponse(
                result.accessToken(),
                result.tokenType(),
                result.expiresInMs(),
                UserResponse.from(result.user())
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User user = getCurrentUserService.execute(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
