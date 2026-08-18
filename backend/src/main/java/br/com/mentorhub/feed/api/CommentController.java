package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.CommentResponse;
import br.com.mentorhub.feed.api.dto.CreateCommentRequest;
import br.com.mentorhub.feed.api.dto.PostCommentsResponse;
import br.com.mentorhub.feed.api.dto.ReplyRequest;
import br.com.mentorhub.feed.application.CreateCommentService;
import br.com.mentorhub.feed.application.DeleteCommentService;
import br.com.mentorhub.feed.application.ListPostCommentsService;
import br.com.mentorhub.feed.application.ReplyCommentService;
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

    private final CreateCommentService createCommentService;
    private final ReplyCommentService replyCommentService;
    private final ListPostCommentsService listPostCommentsService;
    private final DeleteCommentService deleteCommentService;

    public CommentController(
            CreateCommentService createCommentService,
            ReplyCommentService replyCommentService,
            ListPostCommentsService listPostCommentsService,
            DeleteCommentService deleteCommentService
    ) {
        this.createCommentService = createCommentService;
        this.replyCommentService = replyCommentService;
        this.listPostCommentsService = listPostCommentsService;
        this.deleteCommentService = deleteCommentService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        CommentResponse created = createCommentService.execute(
                postId,
                SecurityUtils.requireCurrentUserId(),
                request.content()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/{commentId}/replies")
    public ResponseEntity<CommentResponse> reply(
            @PathVariable UUID postId,
            @PathVariable UUID commentId,
            @Valid @RequestBody ReplyRequest request
    ) {
        CommentResponse created = replyCommentService.execute(
                postId,
                commentId,
                SecurityUtils.requireCurrentUserId(),
                request.content()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PostCommentsResponse> list(@PathVariable UUID postId) {
        return ResponseEntity.ok(listPostCommentsService.execute(postId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID postId, @PathVariable UUID commentId) {
        deleteCommentService.execute(postId, commentId, SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
