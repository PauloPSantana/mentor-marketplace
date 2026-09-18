package br.com.mentorhub.institutions.api;

import br.com.mentorhub.identity.api.dto.AuthTokenResponse;
import br.com.mentorhub.institutions.api.dto.AcceptMentorInvitationRequest;
import br.com.mentorhub.institutions.api.dto.PublicMentorInvitationResponse;
import br.com.mentorhub.institutions.application.AcceptMentorInvitationService;
import br.com.mentorhub.institutions.application.InstitutionDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicInvitationController {

    private final InstitutionDashboardService institutionDashboardService;
    private final AcceptMentorInvitationService acceptMentorInvitationService;

    public PublicInvitationController(
            InstitutionDashboardService institutionDashboardService,
            AcceptMentorInvitationService acceptMentorInvitationService
    ) {
        this.institutionDashboardService = institutionDashboardService;
        this.acceptMentorInvitationService = acceptMentorInvitationService;
    }

    @GetMapping({"/api/v1/mentor-invitations/{token}", "/api/v1/invitations/{token}"})
    public ResponseEntity<PublicMentorInvitationResponse> get(@PathVariable String token) {
        return ResponseEntity.ok(institutionDashboardService.publicInvitation(token));
    }

    @PostMapping("/api/v1/mentor-invitations/{token}/accept")
    public ResponseEntity<AuthTokenResponse> accept(
            @PathVariable String token,
            @Valid @RequestBody AcceptMentorInvitationRequest request
    ) {
        return ResponseEntity.ok(acceptMentorInvitationService.execute(token, request));
    }
}
