package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.PostFeedResponse;
import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.application.PostService;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody PostRequest request) {
        PostResponse created = postService.create(SecurityUtils.requireCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PostFeedResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.listFeed(SecurityUtils.requireCurrentUserId(), page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getById(id, SecurityUtils.requireCurrentUserId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> update(@PathVariable UUID id, @Valid @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.update(id, SecurityUtils.requireCurrentUserId(), request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        postService.delete(id, SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
