package br.com.mentorhub.scheduling.api;

import br.com.mentorhub.integration.google.api.dto.GoogleCalendarEventsResponse;
import br.com.mentorhub.scheduling.api.dto.GoogleConnectResponse;
import br.com.mentorhub.scheduling.api.dto.GoogleStatusResponse;
import br.com.mentorhub.scheduling.application.ConnectGoogleAccountService;
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
@RequestMapping("/api/integrations/google")
public class GoogleIntegrationController {

    private final ConnectGoogleAccountService connectGoogleAccountService;

    public GoogleIntegrationController(ConnectGoogleAccountService connectGoogleAccountService) {
        this.connectGoogleAccountService = connectGoogleAccountService;
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<GoogleStatusResponse> status() {
        return ResponseEntity.ok(connectGoogleAccountService.status(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/connect")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<GoogleConnectResponse> connect() {
        return ResponseEntity.ok(connectGoogleAccountService.authorizationUrl(SecurityUtils.requireCurrentUserId()));
    }

    @DeleteMapping("/connection")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<Void> disconnect() {
        connectGoogleAccountService.disconnect(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/calendar/events")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<GoogleCalendarEventsResponse> calendarEvents() {
        return ResponseEntity.ok(connectGoogleAccountService.listPrimaryEvents(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error
    ) {
        String redirect;
        try {
            if (error != null || code == null || code.isBlank() || state == null || state.isBlank()) {
                redirect = connectGoogleAccountService.frontendErrorUrl();
            } else {
                redirect = connectGoogleAccountService.complete(code, state);
            }
        } catch (RuntimeException ex) {
            redirect = connectGoogleAccountService.frontendErrorUrl();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build();
    }
}
