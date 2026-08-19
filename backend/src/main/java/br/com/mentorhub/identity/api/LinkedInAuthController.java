package br.com.mentorhub.identity.api;

import br.com.mentorhub.identity.api.dto.LinkedInImportedProfileResponse;
import br.com.mentorhub.identity.api.dto.LinkedInPreviewRequest;
import br.com.mentorhub.identity.api.dto.LinkedInPreviewResponse;
import br.com.mentorhub.identity.api.dto.LinkedInStatusResponse;
import br.com.mentorhub.identity.application.LinkedInOAuthService;
import br.com.mentorhub.identity.application.PreviewLinkedInProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/auth/linkedin")
public class LinkedInAuthController {

    private final PreviewLinkedInProfileService previewLinkedInProfileService;
    private final LinkedInOAuthService linkedInOAuthService;

    public LinkedInAuthController(
            PreviewLinkedInProfileService previewLinkedInProfileService,
            LinkedInOAuthService linkedInOAuthService
    ) {
        this.previewLinkedInProfileService = previewLinkedInProfileService;
        this.linkedInOAuthService = linkedInOAuthService;
    }

    @GetMapping("/status")
    public ResponseEntity<LinkedInStatusResponse> status() {
        return ResponseEntity.ok(new LinkedInStatusResponse(linkedInOAuthService.isEnabled()));
    }

    @PostMapping("/preview")
    public ResponseEntity<LinkedInPreviewResponse> preview(@Valid @RequestBody LinkedInPreviewRequest request) {
        return ResponseEntity.ok(previewLinkedInProfileService.execute(request.url()));
    }

    @GetMapping("/start")
    public ResponseEntity<Void> start() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(linkedInOAuthService.authorizationUrl()))
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        String frontend = completeOrError(code, state, error);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(frontend)).build();
    }

    @GetMapping("/import/{token}")
    public ResponseEntity<LinkedInImportedProfileResponse> imported(@PathVariable String token) {
        return ResponseEntity.ok(linkedInOAuthService.consume(token));
    }

    private String completeOrError(String code, String state, String error) {
        if (error != null || code == null || state == null) {
            return linkedInOAuthService.frontendErrorUrl();
        }
        try {
            return linkedInOAuthService.complete(code, state);
        } catch (RuntimeException ignored) {
            return linkedInOAuthService.frontendErrorUrl();
        }
    }
}
