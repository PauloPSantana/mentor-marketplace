package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.CommentRequest;
import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.api.dto.PostCommentsResponse;
import br.com.mentorhub.feed.application.CommentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request
    ) {
        CommentResponse created = commentService.create(postId, SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PostCommentsResponse> list(@PathVariable UUID postId) {
        return ResponseEntity.ok(commentService.listByPost(postId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID postId, @PathVariable UUID commentId) {
        commentService.delete(postId, commentId, SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
