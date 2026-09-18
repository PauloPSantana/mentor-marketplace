package br.com.mentorhub.institutions.api;

import br.com.mentorhub.institutions.api.dto.InstitutionDashboardResponse;
import br.com.mentorhub.institutions.api.dto.InstitutionDashboardResponse.InstitutionInvitationResponse;
import br.com.mentorhub.institutions.api.dto.InviteMentorRequest;
import br.com.mentorhub.institutions.api.dto.MailStatusResponse;
import br.com.mentorhub.institutions.application.InstitutionDashboardService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/institutions")
@PreAuthorize("hasRole('INSTITUTION')")
public class InstitutionController {

    private final InstitutionDashboardService institutionDashboardService;

    public InstitutionController(InstitutionDashboardService institutionDashboardService) {
        this.institutionDashboardService = institutionDashboardService;
    }

    @GetMapping("/me")
    public ResponseEntity<InstitutionDashboardResponse> me() {
        return ResponseEntity.ok(institutionDashboardService.dashboard(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/me/mail-status")
    public ResponseEntity<MailStatusResponse> mailStatus() {
        return ResponseEntity.ok(institutionDashboardService.mailStatus());
    }

    @PostMapping("/me/invitations")
    public ResponseEntity<InstitutionInvitationResponse> invite(@Valid @RequestBody InviteMentorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(institutionDashboardService.invite(SecurityUtils.requireCurrentUserId(), request));
    }

    @PostMapping("/me/invitations/{invitationId}/resend")
    public ResponseEntity<InstitutionInvitationResponse> resend(@PathVariable UUID invitationId) {
        return ResponseEntity.ok(institutionDashboardService.resend(SecurityUtils.requireCurrentUserId(), invitationId));
    }

    @DeleteMapping("/me/invitations/{invitationId}")
    public ResponseEntity<Void> remove(@PathVariable UUID invitationId) {
        institutionDashboardService.remove(SecurityUtils.requireCurrentUserId(), invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/invitations/{invitationId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID invitationId) {
        institutionDashboardService.cancel(SecurityUtils.requireCurrentUserId(), invitationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/mentors/{mentorProfileId}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID mentorProfileId) {
        institutionDashboardService.deactivateMentor(SecurityUtils.requireCurrentUserId(), mentorProfileId);
        return ResponseEntity.noContent().build();
    }
}
