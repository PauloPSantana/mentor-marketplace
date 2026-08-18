package br.com.mentorhub.enrollments.api;

import br.com.mentorhub.enrollments.api.dto.CreateMentorshipRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.application.CancelEnrollmentService;
import br.com.mentorhub.enrollments.application.CreateMentorshipRequestService;
import br.com.mentorhub.enrollments.application.ListMyEnrollmentsService;
import br.com.mentorhub.enrollments.application.UpdateEnrollmentStatusService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorship-requests")
public class MentorshipRequestController {

    private final CreateMentorshipRequestService createMentorshipRequestService;
    private final ListMyEnrollmentsService listMyEnrollmentsService;
    private final UpdateEnrollmentStatusService updateEnrollmentStatusService;
    private final CancelEnrollmentService cancelEnrollmentService;

    public MentorshipRequestController(
            CreateMentorshipRequestService createMentorshipRequestService,
            ListMyEnrollmentsService listMyEnrollmentsService,
            UpdateEnrollmentStatusService updateEnrollmentStatusService,
            CancelEnrollmentService cancelEnrollmentService
    ) {
        this.createMentorshipRequestService = createMentorshipRequestService;
        this.listMyEnrollmentsService = listMyEnrollmentsService;
        this.updateEnrollmentStatusService = updateEnrollmentStatusService;
        this.cancelEnrollmentService = cancelEnrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody CreateMentorshipRequest request) {
        EnrollmentResponse created = createMentorshipRequestService.execute(
                SecurityUtils.requireCurrentUserId(),
                request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/sent")
    public ResponseEntity<List<EnrollmentResponse>> sent() {
        return ResponseEntity.ok(listMyEnrollmentsService.sent(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> received() {
        return ResponseEntity.ok(listMyEnrollmentsService.received(SecurityUtils.requireCurrentUserId()));
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

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<EnrollmentResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(cancelEnrollmentService.execute(SecurityUtils.requireCurrentUserId(), id));
    }
}
