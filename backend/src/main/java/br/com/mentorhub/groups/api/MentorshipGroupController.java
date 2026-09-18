package br.com.mentorhub.groups.api;

import br.com.mentorhub.groups.api.dto.AddGroupMemberRequest;
import br.com.mentorhub.groups.api.dto.CreateGroupRequest;
import br.com.mentorhub.groups.api.dto.GroupCandidateResponse;
import br.com.mentorhub.groups.api.dto.MentorshipGroupResponse;
import br.com.mentorhub.groups.application.AddGroupMemberService;
import br.com.mentorhub.groups.application.CreateGroupService;
import br.com.mentorhub.groups.application.ListGroupCandidatesService;
import br.com.mentorhub.groups.application.ListGroupsService;
import br.com.mentorhub.groups.application.RemoveGroupMemberService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/groups")
public class MentorshipGroupController {

    private final CreateGroupService createGroupService;
    private final ListGroupsService listGroupsService;
    private final ListGroupCandidatesService listGroupCandidatesService;
    private final AddGroupMemberService addGroupMemberService;
    private final RemoveGroupMemberService removeGroupMemberService;

    public MentorshipGroupController(
            CreateGroupService createGroupService,
            ListGroupsService listGroupsService,
            ListGroupCandidatesService listGroupCandidatesService,
            AddGroupMemberService addGroupMemberService,
            RemoveGroupMemberService removeGroupMemberService
    ) {
        this.createGroupService = createGroupService;
        this.listGroupsService = listGroupsService;
        this.listGroupCandidatesService = listGroupCandidatesService;
        this.addGroupMemberService = addGroupMemberService;
        this.removeGroupMemberService = removeGroupMemberService;
    }

    @GetMapping
    public ResponseEntity<List<MentorshipGroupResponse>> list() {
        return ResponseEntity.ok(listGroupsService.execute(SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping("/candidates")
    public ResponseEntity<List<GroupCandidateResponse>> candidates() {
        return ResponseEntity.ok(listGroupCandidatesService.forCreate(SecurityUtils.requireCurrentUserId()));
    }

    @PostMapping
    public ResponseEntity<MentorshipGroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createGroupService.execute(SecurityUtils.requireCurrentUserId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorshipGroupResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(listGroupsService.getById(SecurityUtils.requireCurrentUserId(), id));
    }

    @GetMapping("/{id}/candidates")
    public ResponseEntity<List<GroupCandidateResponse>> groupCandidates(@PathVariable UUID id) {
        return ResponseEntity.ok(listGroupCandidatesService.forGroup(SecurityUtils.requireCurrentUserId(), id));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<MentorshipGroupResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddGroupMemberRequest request
    ) {
        return ResponseEntity.ok(addGroupMemberService.execute(SecurityUtils.requireCurrentUserId(), id, request));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<MentorshipGroupResponse> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        return ResponseEntity.ok(removeGroupMemberService.execute(SecurityUtils.requireCurrentUserId(), id, userId));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<MentorshipGroupResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(removeGroupMemberService.close(SecurityUtils.requireCurrentUserId(), id));
    }
}
