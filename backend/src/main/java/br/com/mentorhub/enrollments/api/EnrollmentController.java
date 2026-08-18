package br.com.mentorhub.enrollments.api;

import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.application.CancelEnrollmentService;
import br.com.mentorhub.enrollments.application.ListMyEnrollmentsService;
import br.com.mentorhub.enrollments.application.UpdateEnrollmentStatusService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final ListMyEnrollmentsService listMyEnrollmentsService;
    private final UpdateEnrollmentStatusService updateEnrollmentStatusService;
    private final CancelEnrollmentService cancelEnrollmentService;

    public EnrollmentController(
            ListMyEnrollmentsService listMyEnrollmentsService,
            UpdateEnrollmentStatusService updateEnrollmentStatusService,
            CancelEnrollmentService cancelEnrollmentService
    ) {
        this.listMyEnrollmentsService = listMyEnrollmentsService;
        this.updateEnrollmentStatusService = updateEnrollmentStatusService;
        this.cancelEnrollmentService = cancelEnrollmentService;
    }

    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponse>> me() {
        return ResponseEntity.ok(listMyEnrollmentsService.execute(SecurityUtils.requireCurrentUserId()));
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> accept(@PathVariable UUID id) {
        return ResponseEntity.ok(updateEnrollmentStatusService.accept(SecurityUtils.requireCurrentUserId(), id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> reject(@PathVariable UUID id) {
        return ResponseEntity.ok(updateEnrollmentStatusService.reject(SecurityUtils.requireCurrentUserId(), id));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(updateEnrollmentStatusService.complete(SecurityUtils.requireCurrentUserId(), id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(cancelEnrollmentService.execute(SecurityUtils.requireCurrentUserId(), id));
    }
}
