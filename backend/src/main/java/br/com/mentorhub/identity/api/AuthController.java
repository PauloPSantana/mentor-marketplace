package br.com.mentorhub.identity.api;

import br.com.mentorhub.identity.api.dto.AuthTokenResponse;
import br.com.mentorhub.identity.api.dto.LoginRequest;
import br.com.mentorhub.identity.api.dto.RegisterRequest;
import br.com.mentorhub.identity.api.dto.UpdateCurrentUserRequest;
import br.com.mentorhub.identity.api.dto.UserResponse;
import br.com.mentorhub.identity.application.GetCurrentUserService;
import br.com.mentorhub.identity.application.LoginUserService;
import br.com.mentorhub.identity.application.RegisterUserService;
import br.com.mentorhub.identity.application.UpdateCurrentUserService;
import br.com.mentorhub.identity.application.UploadProfilePhotoService;
import br.com.mentorhub.identity.domain.User;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserService registerUserService;
    private final LoginUserService loginUserService;
    private final GetCurrentUserService getCurrentUserService;
    private final UpdateCurrentUserService updateCurrentUserService;
    private final UploadProfilePhotoService uploadProfilePhotoService;

    public AuthController(
            RegisterUserService registerUserService,
            LoginUserService loginUserService,
            GetCurrentUserService getCurrentUserService,
            UpdateCurrentUserService updateCurrentUserService,
            UploadProfilePhotoService uploadProfilePhotoService
    ) {
        this.registerUserService = registerUserService;
        this.loginUserService = loginUserService;
        this.getCurrentUserService = getCurrentUserService;
        this.updateCurrentUserService = updateCurrentUserService;
        this.uploadProfilePhotoService = uploadProfilePhotoService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUserService.execute(
                request.name(),
                request.email(),
                request.password(),
                request.confirmPassword(),
                request.role(),
                request.linkedinUrl(),
                request.photoUrl(),
                request.invitationToken(),
                request.institutionName()
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

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@Valid @RequestBody UpdateCurrentUserRequest request) {
        User user = updateCurrentUserService.execute(SecurityUtils.requireCurrentUserId(), request.name());
        return ResponseEntity.ok(UserResponse.from(user));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadPhoto(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("INVALID_PHOTO", "Selecione uma foto para enviar");
        }
        try {
            User user = uploadProfilePhotoService.execute(SecurityUtils.requireCurrentUserId(), file.getBytes());
            return ResponseEntity.ok(UserResponse.from(user));
        } catch (IOException ex) {
            throw new BusinessException("INVALID_PHOTO", "Não foi possível ler a foto enviada");
        }
    }
}
