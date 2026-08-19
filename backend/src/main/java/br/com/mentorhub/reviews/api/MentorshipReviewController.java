package br.com.mentorhub.reviews.api;

import br.com.mentorhub.reviews.api.dto.CreateReviewRequest;
import br.com.mentorhub.reviews.api.dto.ReviewResponse;
import br.com.mentorhub.reviews.application.CreateReviewService;
import br.com.mentorhub.reviews.application.ListMentorshipReviewsService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mentorships/relationships/{id}/reviews")
public class MentorshipReviewController {

    private final CreateReviewService createReviewService;
    private final ListMentorshipReviewsService listMentorshipReviewsService;

    public MentorshipReviewController(
            CreateReviewService createReviewService,
            ListMentorshipReviewsService listMentorshipReviewsService
    ) {
        this.createReviewService = createReviewService;
        this.listMentorshipReviewsService = listMentorshipReviewsService;
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(
            @PathVariable UUID id,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        ReviewResponse saved = createReviewService.execute(SecurityUtils.requireCurrentUserId(), id, request);
        boolean created = saved.createdAt().equals(saved.updatedAt());
        return ResponseEntity.status(created ? HttpStatus.CREATED : HttpStatus.OK).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(listMentorshipReviewsService.execute(SecurityUtils.requireCurrentUserId(), id));
    }
}
