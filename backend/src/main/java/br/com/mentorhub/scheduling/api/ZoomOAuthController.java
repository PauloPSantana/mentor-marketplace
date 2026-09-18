package br.com.mentorhub.scheduling.api;

import br.com.mentorhub.scheduling.api.dto.ZoomConnectResponse;
import br.com.mentorhub.scheduling.api.dto.ZoomStatusResponse;
import br.com.mentorhub.scheduling.application.ZoomOAuthService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/zoom")
public class ZoomOAuthController {

    private final ZoomOAuthService zoomOAuthService;

    public ZoomOAuthController(ZoomOAuthService zoomOAuthService) {
        this.zoomOAuthService = zoomOAuthService;
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ZoomStatusResponse> status() {
        return ResponseEntity.ok(zoomOAuthService.status(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/connect")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ZoomConnectResponse> connect() {
        return ResponseEntity.ok(zoomOAuthService.authorizationUrl(SecurityUtils.requireCurrentUserId()));
    }

    @DeleteMapping("/connection")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Void> disconnect() {
        zoomOAuthService.disconnect(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        String redirect;
        try {
            if (error != null || code == null || state == null) {
                redirect = zoomOAuthService.frontendErrorUrl();
            } else {
                redirect = zoomOAuthService.complete(code, state);
            }
        } catch (RuntimeException ex) {
            redirect = zoomOAuthService.frontendErrorUrl();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
    }
}
