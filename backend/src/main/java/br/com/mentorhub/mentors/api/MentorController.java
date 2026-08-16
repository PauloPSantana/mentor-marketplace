package br.com.mentorhub.mentors.api;

import br.com.mentorhub.mentors.api.dto.MentorProfileResponse;
import br.com.mentorhub.mentors.api.dto.UpdateMentorProfileRequest;
import br.com.mentorhub.mentors.application.GetMentorProfileService;
import br.com.mentorhub.mentors.application.GetMyMentorProfileService;
import br.com.mentorhub.mentors.application.ListMentorProfilesService;
import br.com.mentorhub.mentors.application.UpdateMyMentorProfileService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentors")
public class MentorController {

    private final ListMentorProfilesService listMentorProfilesService;
    private final GetMentorProfileService getMentorProfileService;
    private final GetMyMentorProfileService getMyMentorProfileService;
    private final UpdateMyMentorProfileService updateMyMentorProfileService;

    public MentorController(
            ListMentorProfilesService listMentorProfilesService,
            GetMentorProfileService getMentorProfileService,
            GetMyMentorProfileService getMyMentorProfileService,
            UpdateMyMentorProfileService updateMyMentorProfileService
    ) {
        this.listMentorProfilesService = listMentorProfilesService;
        this.getMentorProfileService = getMentorProfileService;
        this.getMyMentorProfileService = getMyMentorProfileService;
        this.updateMyMentorProfileService = updateMyMentorProfileService;
    }

    @GetMapping
    public ResponseEntity<List<MentorProfileResponse>> list() {
        return ResponseEntity.ok(listMentorProfilesService.execute());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<MentorProfileResponse> me() {
        return ResponseEntity.ok(getMyMentorProfileService.execute(SecurityUtils.requireCurrentUserId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<MentorProfileResponse> updateMe(@Valid @RequestBody UpdateMentorProfileRequest request) {
        return ResponseEntity.ok(updateMyMentorProfileService.execute(SecurityUtils.requireCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorProfileResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getMentorProfileService.execute(id));
    }
}
