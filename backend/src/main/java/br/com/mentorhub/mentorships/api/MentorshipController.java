package br.com.mentorhub.mentorships.api;

import br.com.mentorhub.mentorships.api.dto.CreateSessionRequest;
import br.com.mentorhub.mentorships.api.dto.MentorshipPageResponse;
import br.com.mentorhub.mentorships.api.dto.MentorshipProductResponse;
import br.com.mentorhub.mentorships.api.dto.MentorshipRelationshipResponse;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.mentorships.application.CreateSessionService;
import br.com.mentorhub.mentorships.application.ListMentorshipSessionsService;
import br.com.mentorhub.mentorships.application.ListMentorshipsService;
import br.com.mentorhub.mentorships.application.ListMentorshipProductsService;
import br.com.mentorhub.mentorships.application.UpdateMentorshipStatusService;
import br.com.mentorhub.mentorships.domain.MentorshipStatus;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorships")
public class MentorshipController {

    private final ListMentorshipProductsService listMentorshipProductsService;
    private final ListMentorshipsService listMentorshipsService;
    private final UpdateMentorshipStatusService updateMentorshipStatusService;
    private final CreateSessionService createSessionService;
    private final ListMentorshipSessionsService listMentorshipSessionsService;

    public MentorshipController(
            ListMentorshipProductsService listMentorshipProductsService,
            ListMentorshipsService listMentorshipsService,
            UpdateMentorshipStatusService updateMentorshipStatusService,
            CreateSessionService createSessionService,
            ListMentorshipSessionsService listMentorshipSessionsService
    ) {
        this.listMentorshipProductsService = listMentorshipProductsService;
        this.listMentorshipsService = listMentorshipsService;
        this.updateMentorshipStatusService = updateMentorshipStatusService;
        this.createSessionService = createSessionService;
        this.listMentorshipSessionsService = listMentorshipSessionsService;
    }

    @GetMapping
    public ResponseEntity<List<MentorshipProductResponse>> list(@RequestParam(required = false) UUID mentorId) {
        return ResponseEntity.ok(listMentorshipProductsService.execute(mentorId));
    }

    @GetMapping("/as-mentor")
    public ResponseEntity<MentorshipPageResponse> asMentor(
            @RequestParam(required = false) MentorshipStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listMentorshipsService.asMentor(SecurityUtils.requireCurrentUserId(), status, page, size));
    }

    @GetMapping("/as-mentee")
    public ResponseEntity<MentorshipPageResponse> asMentee(
            @RequestParam(required = false) MentorshipStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listMentorshipsService.asMentee(SecurityUtils.requireCurrentUserId(), status, page, size));
    }

    @GetMapping("/relationships/{id}")
    public ResponseEntity<MentorshipRelationshipResponse> relationship(@PathVariable UUID id) {
        return ResponseEntity.ok(listMentorshipsService.getById(SecurityUtils.requireCurrentUserId(), id));
    }

    @PatchMapping("/relationships/{id}/complete")
    public ResponseEntity<MentorshipRelationshipResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(updateMentorshipStatusService.complete(SecurityUtils.requireCurrentUserId(), id));
    }

    @PatchMapping("/relationships/{id}/cancel")
    public ResponseEntity<MentorshipRelationshipResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(updateMentorshipStatusService.cancel(SecurityUtils.requireCurrentUserId(), id));
    }

    @GetMapping("/relationships/{id}/sessions")
    public ResponseEntity<List<MentorshipSessionResponse>> sessions(@PathVariable UUID id) {
        return ResponseEntity.ok(listMentorshipSessionsService.execute(SecurityUtils.requireCurrentUserId(), id));
    }

    @PostMapping("/relationships/{id}/sessions")
    public ResponseEntity<MentorshipSessionResponse> createSession(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSessionRequest request
    ) {
        MentorshipSessionResponse created = createSessionService.execute(SecurityUtils.requireCurrentUserId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorshipProductResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(listMentorshipProductsService.getById(id));
    }
}
