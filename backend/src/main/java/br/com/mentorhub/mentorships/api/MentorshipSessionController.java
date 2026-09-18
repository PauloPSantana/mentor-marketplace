package br.com.mentorhub.mentorships.api;

import br.com.mentorhub.mentorships.api.dto.CancelSessionRequest;
import br.com.mentorhub.mentorships.api.dto.CompleteSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.api.dto.RescheduleSessionRequest;
import br.com.mentorhub.mentorships.api.dto.SessionStatsResponse;
import br.com.mentorhub.mentorships.application.CancelSessionService;
import br.com.mentorhub.mentorships.application.CompleteSessionService;
import br.com.mentorhub.mentorships.application.ListSessionStatsService;
import br.com.mentorhub.mentorships.application.MarkSessionNoShowService;
import br.com.mentorhub.mentorships.application.RescheduleSessionService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class MentorshipSessionController {

    private final CompleteSessionService completeSessionService;
    private final CancelSessionService cancelSessionService;
    private final MarkSessionNoShowService markSessionNoShowService;
    private final RescheduleSessionService rescheduleSessionService;
    private final ListSessionStatsService listSessionStatsService;

    public MentorshipSessionController(
            CompleteSessionService completeSessionService,
            CancelSessionService cancelSessionService,
            MarkSessionNoShowService markSessionNoShowService,
            RescheduleSessionService rescheduleSessionService,
            ListSessionStatsService listSessionStatsService
    ) {
        this.completeSessionService = completeSessionService;
        this.cancelSessionService = cancelSessionService;
        this.markSessionNoShowService = markSessionNoShowService;
        this.rescheduleSessionService = rescheduleSessionService;
        this.listSessionStatsService = listSessionStatsService;
    }

    @GetMapping("/stats")
    public ResponseEntity<SessionStatsResponse> stats() {
        return ResponseEntity.ok(listSessionStatsService.execute(SecurityUtils.requireCurrentUserId()));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<MentorshipSessionResponse> reschedule(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleSessionRequest request
    ) {
        return ResponseEntity.ok(rescheduleSessionService.execute(SecurityUtils.requireCurrentUserId(), id, request));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<MentorshipSessionResponse> complete(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CompleteSessionRequest request
    ) {
        return ResponseEntity.ok(completeSessionService.execute(
                SecurityUtils.requireCurrentUserId(),
                id,
                request == null ? new CompleteSessionRequest(null) : request
        ));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<MentorshipSessionResponse> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelSessionRequest request
    ) {
        return ResponseEntity.ok(cancelSessionService.execute(
                SecurityUtils.requireCurrentUserId(),
                id,
                request == null ? new CancelSessionRequest(null) : request
        ));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<MentorshipSessionResponse> noShow(@PathVariable UUID id) {
        return ResponseEntity.ok(markSessionNoShowService.execute(SecurityUtils.requireCurrentUserId(), id));
    }
}
