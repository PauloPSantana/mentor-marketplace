package br.com.mentorhub.social.api;

import br.com.mentorhub.shared.security.SecurityUtils;
import br.com.mentorhub.social.api.dto.BlockStatusResponse;
import br.com.mentorhub.social.api.dto.FollowListResponse;
import br.com.mentorhub.social.api.dto.FollowStatusResponse;
import br.com.mentorhub.social.application.BlockUserService;
import br.com.mentorhub.social.application.FollowUserService;
import br.com.mentorhub.social.application.GetBlockStatusService;
import br.com.mentorhub.social.application.GetFollowStatusService;
import br.com.mentorhub.social.application.ListFollowersService;
import br.com.mentorhub.social.application.ListFollowingService;
import br.com.mentorhub.social.application.UnblockUserService;
import br.com.mentorhub.social.application.UnfollowUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}")
public class UserFollowController {

    private final FollowUserService followUserService;
    private final UnfollowUserService unfollowUserService;
    private final GetFollowStatusService getFollowStatusService;
    private final ListFollowersService listFollowersService;
    private final ListFollowingService listFollowingService;
    private final BlockUserService blockUserService;
    private final UnblockUserService unblockUserService;
    private final GetBlockStatusService getBlockStatusService;

    public UserFollowController(
            FollowUserService followUserService,
            UnfollowUserService unfollowUserService,
            GetFollowStatusService getFollowStatusService,
            ListFollowersService listFollowersService,
            ListFollowingService listFollowingService,
            BlockUserService blockUserService,
            UnblockUserService unblockUserService,
            GetBlockStatusService getBlockStatusService
    ) {
        this.followUserService = followUserService;
        this.unfollowUserService = unfollowUserService;
        this.getFollowStatusService = getFollowStatusService;
        this.listFollowersService = listFollowersService;
        this.listFollowingService = listFollowingService;
        this.blockUserService = blockUserService;
        this.unblockUserService = unblockUserService;
        this.getBlockStatusService = getBlockStatusService;
    }

    @PostMapping("/follow")
    public ResponseEntity<FollowStatusResponse> follow(@PathVariable UUID userId) {
        return ResponseEntity.ok(followUserService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @DeleteMapping("/follow")
    public ResponseEntity<FollowStatusResponse> unfollow(@PathVariable UUID userId) {
        return ResponseEntity.ok(unfollowUserService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @GetMapping("/follow-status")
    public ResponseEntity<FollowStatusResponse> followStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(getFollowStatusService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @PostMapping("/block")
    public ResponseEntity<BlockStatusResponse> block(@PathVariable UUID userId) {
        return ResponseEntity.ok(blockUserService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @DeleteMapping("/block")
    public ResponseEntity<BlockStatusResponse> unblock(@PathVariable UUID userId) {
        return ResponseEntity.ok(unblockUserService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @GetMapping("/block-status")
    public ResponseEntity<BlockStatusResponse> blockStatus(@PathVariable UUID userId) {
        return ResponseEntity.ok(getBlockStatusService.execute(SecurityUtils.requireCurrentUserId(), userId));
    }

    @GetMapping("/followers")
    public ResponseEntity<FollowListResponse> followers(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listFollowersService.execute(userId, page, size));
    }

    @GetMapping("/following")
    public ResponseEntity<FollowListResponse> following(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listFollowingService.execute(userId, page, size));
    }
}
