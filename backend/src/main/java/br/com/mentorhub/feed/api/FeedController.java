package br.com.mentorhub.feed.api;

import br.com.mentorhub.feed.api.dto.PersonalizedFeedResponse;
import br.com.mentorhub.feed.application.PersonalizedFeedService;
import br.com.mentorhub.feed.domain.FeedType;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final PersonalizedFeedService personalizedFeedService;

    public FeedController(PersonalizedFeedService personalizedFeedService) {
        this.personalizedFeedService = personalizedFeedService;
    }

    @GetMapping
    public ResponseEntity<PersonalizedFeedResponse> list(
            @RequestParam(defaultValue = "FOR_YOU") FeedType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                personalizedFeedService.execute(SecurityUtils.requireCurrentUserId(), type, page, size)
        );
    }
}
