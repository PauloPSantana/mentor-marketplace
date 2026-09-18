package br.com.mentorhub.announcements.api;

import br.com.mentorhub.announcements.api.dto.AnnouncementCommentResponse;
import br.com.mentorhub.announcements.api.dto.AnnouncementCommentsResponse;
import br.com.mentorhub.announcements.api.dto.AnnouncementLikeResponse;
import br.com.mentorhub.announcements.api.dto.AnnouncementResponse;
import br.com.mentorhub.announcements.api.dto.CreateAnnouncementCommentRequest;
import br.com.mentorhub.announcements.api.dto.CreateAnnouncementRequest;
import br.com.mentorhub.announcements.application.CreateAnnouncementCommentService;
import br.com.mentorhub.announcements.application.CreateAnnouncementService;
import br.com.mentorhub.announcements.application.DeleteAnnouncementCommentService;
import br.com.mentorhub.announcements.application.LikeAnnouncementService;
import br.com.mentorhub.announcements.application.ListAnnouncementCommentsService;
import br.com.mentorhub.announcements.application.ListAnnouncementsService;
import br.com.mentorhub.announcements.application.UnlikeAnnouncementService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/announcements")
public class AnnouncementController {

    private final CreateAnnouncementService createAnnouncementService;
    private final ListAnnouncementsService listAnnouncementsService;
    private final LikeAnnouncementService likeAnnouncementService;
    private final UnlikeAnnouncementService unlikeAnnouncementService;
    private final CreateAnnouncementCommentService createAnnouncementCommentService;
    private final ListAnnouncementCommentsService listAnnouncementCommentsService;
    private final DeleteAnnouncementCommentService deleteAnnouncementCommentService;

    public AnnouncementController(
            CreateAnnouncementService createAnnouncementService,
            ListAnnouncementsService listAnnouncementsService,
            LikeAnnouncementService likeAnnouncementService,
            UnlikeAnnouncementService unlikeAnnouncementService,
            CreateAnnouncementCommentService createAnnouncementCommentService,
            ListAnnouncementCommentsService listAnnouncementCommentsService,
            DeleteAnnouncementCommentService deleteAnnouncementCommentService
    ) {
        this.createAnnouncementService = createAnnouncementService;
        this.listAnnouncementsService = listAnnouncementsService;
        this.likeAnnouncementService = likeAnnouncementService;
        this.unlikeAnnouncementService = unlikeAnnouncementService;
        this.createAnnouncementCommentService = createAnnouncementCommentService;
        this.listAnnouncementCommentsService = listAnnouncementCommentsService;
        this.deleteAnnouncementCommentService = deleteAnnouncementCommentService;
    }

    @GetMapping
    public ResponseEntity<List<AnnouncementResponse>> list(@PathVariable UUID groupId) {
        return ResponseEntity.ok(listAnnouncementsService.execute(SecurityUtils.requireCurrentUserId(), groupId));
    }

    @PostMapping
    public ResponseEntity<AnnouncementResponse> create(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateAnnouncementRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createAnnouncementService.execute(SecurityUtils.requireCurrentUserId(), groupId, request));
    }

    @PostMapping("/{announcementId}/likes")
    public ResponseEntity<AnnouncementLikeResponse> like(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId
    ) {
        return ResponseEntity.ok(likeAnnouncementService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId
        ));
    }

    @DeleteMapping("/{announcementId}/likes")
    public ResponseEntity<AnnouncementLikeResponse> unlike(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId
    ) {
        return ResponseEntity.ok(unlikeAnnouncementService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId
        ));
    }

    @GetMapping("/{announcementId}/comments")
    public ResponseEntity<AnnouncementCommentsResponse> comments(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId
    ) {
        return ResponseEntity.ok(listAnnouncementCommentsService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId
        ));
    }

    @PostMapping("/{announcementId}/comments")
    public ResponseEntity<AnnouncementCommentResponse> comment(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId,
            @Valid @RequestBody CreateAnnouncementCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createAnnouncementCommentService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId,
                request.content(),
                null
        ));
    }

    @PostMapping("/{announcementId}/comments/{commentId}/replies")
    public ResponseEntity<AnnouncementCommentResponse> reply(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateAnnouncementCommentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(createAnnouncementCommentService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId,
                request.content(),
                commentId
        ));
    }

    @DeleteMapping("/{announcementId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID groupId,
            @PathVariable UUID announcementId,
            @PathVariable UUID commentId
    ) {
        deleteAnnouncementCommentService.execute(
                SecurityUtils.requireCurrentUserId(),
                groupId,
                announcementId,
                commentId
        );
        return ResponseEntity.noContent().build();
    }
}
