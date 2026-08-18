package br.com.mentorhub.enrollments.api;

import br.com.mentorhub.enrollments.api.dto.EnrollmentRequest;
import br.com.mentorhub.enrollments.api.dto.EnrollmentResponse;
import br.com.mentorhub.enrollments.application.RequestEnrollmentService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorships/{id}/enrollments")
public class MentorshipEnrollmentController {

    private final RequestEnrollmentService requestEnrollmentService;

    public MentorshipEnrollmentController(RequestEnrollmentService requestEnrollmentService) {
        this.requestEnrollmentService = requestEnrollmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) EnrollmentRequest request
    ) {
        EnrollmentResponse created = requestEnrollmentService.execute(
                SecurityUtils.requireCurrentUserId(),
                id,
                request == null ? new EnrollmentRequest(null) : request
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
