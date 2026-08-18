package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.PostLikeResponse;
import br.com.mentorhub.feed.api.dto.PostLikeUserResponse;
import br.com.mentorhub.feed.application.GetPostLikeService;
import br.com.mentorhub.feed.application.LikePostService;
import br.com.mentorhub.feed.application.ListPostLikesService;
import br.com.mentorhub.feed.application.UnlikePostService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
public class PostLikeController {

    private final GetPostLikeService getPostLikeService;
    private final ListPostLikesService listPostLikesService;
    private final LikePostService likePostService;
    private final UnlikePostService unlikePostService;

    public PostLikeController(
            GetPostLikeService getPostLikeService,
            ListPostLikesService listPostLikesService,
            LikePostService likePostService,
            UnlikePostService unlikePostService
    ) {
        this.getPostLikeService = getPostLikeService;
        this.listPostLikesService = listPostLikesService;
        this.likePostService = likePostService;
        this.unlikePostService = unlikePostService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<PostLikeUserResponse>> listUsers(@PathVariable UUID postId) {
        return ResponseEntity.ok(listPostLikesService.execute(postId, SecurityUtils.requireCurrentUserId()));
    }

    @GetMapping
    public ResponseEntity<PostLikeResponse> get(@PathVariable UUID postId) {
        return ResponseEntity.ok(getPostLikeService.execute(postId, SecurityUtils.requireCurrentUserId()));
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
