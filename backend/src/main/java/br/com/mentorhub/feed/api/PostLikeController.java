package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.application.LikePostService;
import br.com.mentorhub.feed.application.UnlikePostService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class PostLikeController {

    private final LikePostService likePostService;
    private final UnlikePostService unlikePostService;

    public PostLikeController(LikePostService likePostService, UnlikePostService unlikePostService) {
        this.likePostService = likePostService;
        this.unlikePostService = unlikePostService;
    }

    @PostMapping
    public ResponseEntity<PostLikeResponse> like(@PathVariable UUID postId) {
        return ResponseEntity.ok(likePostService.execute(postId, SecurityUtils.requireCurrentUserId()));
    }

    @DeleteMapping
    public ResponseEntity<PostLikeResponse> unlike(@PathVariable UUID postId) {
        return ResponseEntity.ok(unlikePostService.execute(postId, SecurityUtils.requireCurrentUserId()));
    }
}
