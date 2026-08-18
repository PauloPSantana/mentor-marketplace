package br.com.mentorhub.enrollments.api;

import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.application.GetMentorEnrollmentStatusService;
import br.com.mentorhub.enrollments.application.RequestEnrollmentFromMentorService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors/{mentorId}/enrollments")
public class MentorEnrollmentController {

    private final RequestEnrollmentFromMentorService requestEnrollmentFromMentorService;
    private final GetMentorEnrollmentStatusService getMentorEnrollmentStatusService;

    public MentorEnrollmentController(
            RequestEnrollmentFromMentorService requestEnrollmentFromMentorService,
            GetMentorEnrollmentStatusService getMentorEnrollmentStatusService
    ) {
        this.requestEnrollmentFromMentorService = requestEnrollmentFromMentorService;
        this.getMentorEnrollmentStatusService = getMentorEnrollmentStatusService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<EnrollmentResponse> request(
            @PathVariable UUID mentorId,
            @Valid @RequestBody(required = false) EnrollmentRequest request
    ) {
        EnrollmentResponse created = requestEnrollmentFromMentorService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorId,
                request == null ? new EnrollmentRequest(null) : request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public ResponseEntity<EnrollmentResponse> myStatus(@PathVariable UUID mentorId) {
        return getMentorEnrollmentStatusService.execute(SecurityUtils.requireCurrentUserId(), mentorId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
