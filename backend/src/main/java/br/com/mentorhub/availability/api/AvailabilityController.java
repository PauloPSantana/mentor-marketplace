package br.com.mentorhub.availability.api;

import br.com.mentorhub.availability.api.dto.BookMentorSlotRequest;
import br.com.mentorhub.availability.api.dto.MentorAvailabilityResponse;
import br.com.mentorhub.availability.api.dto.UpsertMentorAvailabilityRequest;
import br.com.mentorhub.availability.application.BookMentorSlotService;
import br.com.mentorhub.availability.application.ListMentorAvailabilityService;
import br.com.mentorhub.availability.application.UpdateMentorAvailabilityService;
import br.com.mentorhub.mentorships.api.dto.MentorshipSessionResponse;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors/{mentorId}")
public class AvailabilityController {

    private final ListMentorAvailabilityService listMentorAvailabilityService;
    private final UpdateMentorAvailabilityService updateMentorAvailabilityService;
    private final BookMentorSlotService bookMentorSlotService;

    public AvailabilityController(
            ListMentorAvailabilityService listMentorAvailabilityService,
            UpdateMentorAvailabilityService updateMentorAvailabilityService,
            BookMentorSlotService bookMentorSlotService
    ) {
        this.listMentorAvailabilityService = listMentorAvailabilityService;
        this.updateMentorAvailabilityService = updateMentorAvailabilityService;
        this.bookMentorSlotService = bookMentorSlotService;
    }

    @GetMapping("/availability")
    public ResponseEntity<MentorAvailabilityResponse> get(
            @PathVariable UUID mentorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(listMentorAvailabilityService.execute(mentorId, from, to));
    }

    @PutMapping("/availability")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<MentorAvailabilityResponse> upsert(
            @PathVariable UUID mentorId,
            @Valid @RequestBody UpsertMentorAvailabilityRequest request
    ) {
        return ResponseEntity.ok(updateMentorAvailabilityService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorId,
                request
        ));
    }

    @PostMapping("/bookings")
    public ResponseEntity<MentorshipSessionResponse> book(
            @PathVariable UUID mentorId,
            @Valid @RequestBody BookMentorSlotRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMentorSlotService.execute(
                SecurityUtils.requireCurrentUserId(),
                mentorId,
                request
        ));
    }
}
