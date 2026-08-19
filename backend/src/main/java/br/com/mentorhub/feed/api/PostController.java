package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.CreatePostRequest;
import br.com.mentorhub.feed.api.dto.PostFeedResponse;
import br.com.mentorhub.feed.api.dto.PostRequest;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.application.CreatePostService;
import br.com.mentorhub.feed.application.GetPostService;
import br.com.mentorhub.feed.application.ListFeedService;
import br.com.mentorhub.feed.application.PostResponseMapper;
import br.com.mentorhub.feed.application.PostService;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.application.PostImageStorage;
import br.com.mentorhub.shared.exception.BusinessException;
import br.com.mentorhub.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final CreatePostService createPostService;
    private final GetPostService getPostService;
    private final ListFeedService listFeedService;
    private final PostResponseMapper postResponseMapper;
    private final PostService postService;
    private final PostImageStorage postImageStorage;

    public PostController(
            CreatePostService createPostService,
            GetPostService getPostService,
            ListFeedService listFeedService,
            PostResponseMapper postResponseMapper,
            PostService postService,
            PostImageStorage postImageStorage
    ) {
        this.createPostService = createPostService;
        this.getPostService = getPostService;
        this.listFeedService = listFeedService;
        this.postResponseMapper = postResponseMapper;
        this.postService = postService;
        this.postImageStorage = postImageStorage;
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@Valid @RequestBody CreatePostRequest request) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        Post post = createPostService.execute(userId, request.content(), request.imageUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(postResponseMapper.toResponse(post, userId));
    }

    @GetMapping
    public ResponseEntity<PostFeedResponse> feed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        Page<Post> feed = listFeedService.execute(page, size);
        return ResponseEntity.ok(new PostFeedResponse(
                postResponseMapper.toResponses(feed.getContent(), userId),
                feed.getNumber(),
                feed.getSize(),
                feed.getTotalElements(),
                feed.getTotalPages(),
                feed.isLast()
        ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> findById(@PathVariable UUID postId) {
        UUID userId = SecurityUtils.requireCurrentUserId();
        Post post = getPostService.execute(postId);
        return ResponseEntity.ok(postResponseMapper.toResponse(post, userId));
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

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("INVALID_IMAGE", "Selecione uma imagem para enviar");
        }
        try {
            String url = postImageStorage.save(file.getBytes());
            return ResponseEntity.ok(new ImageUploadResponse(url));
        } catch (IOException ex) {
            throw new BusinessException("INVALID_IMAGE", "Não foi possível ler a imagem enviada");
        }
    }

    public record ImageUploadResponse(String url) {
    }
}
