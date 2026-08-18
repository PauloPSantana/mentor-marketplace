package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.api.dto.FeedPostResponse;
import br.com.mentorhub.feed.api.dto.PersonalizedFeedResponse;
import br.com.mentorhub.feed.api.dto.PostResponse;
import br.com.mentorhub.feed.domain.CommentRepository;
import br.com.mentorhub.feed.domain.FeedType;
import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.feed.domain.PostLikeRepository;
import br.com.mentorhub.feed.domain.PostRepository;
import br.com.mentorhub.mentors.domain.MentorProfile;
import br.com.mentorhub.mentors.domain.MentorProfileRepository;
import br.com.mentorhub.social.domain.UserBlockRepository;
import br.com.mentorhub.social.domain.UserFollowRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PersonalizedFeedService {

    static final int DEFAULT_PAGE_SIZE = 10;
    static final int MAX_PAGE_SIZE = 20;
    static final int FOR_YOU_CANDIDATE_LIMIT = 200;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserBlockRepository userBlockRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public PersonalizedFeedService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            CommentRepository commentRepository,
            UserFollowRepository userFollowRepository,
            UserBlockRepository userBlockRepository,
            MentorProfileRepository mentorProfileRepository
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userFollowRepository = userFollowRepository;
        this.userBlockRepository = userBlockRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @Transactional(readOnly = true)
    public PersonalizedFeedResponse execute(UUID currentUserId, FeedType type, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);

        return switch (type) {
            case RECENT -> listRecent(currentUserId, safePage, safeSize, FeedType.RECENT);
            case FOLLOWING -> listFollowing(currentUserId, safePage, safeSize);
            case FOR_YOU -> listForYou(currentUserId, safePage, safeSize);
        };
    }

    private PersonalizedFeedResponse listRecent(UUID currentUserId, int page, int size, FeedType type) {
        Page<Post> feed = postRepository.findFeed(PageRequest.of(page, size));
        Set<UUID> followedAuthorIds = new HashSet<>(userFollowRepository.findAllFollowedIdsByFollowerId(currentUserId));
        List<Post> visiblePosts = excludeBlockedAuthors(feed.getContent(), currentUserId);
        List<FeedPostResponse> items = toFeedResponses(visiblePosts, currentUserId, followedAuthorIds, null);
        return new PersonalizedFeedResponse(
                items,
                feed.getNumber(),
                feed.getSize(),
                feed.getTotalElements(),
                feed.getTotalPages(),
                feed.isLast(),
                type
        );
    }

    private PersonalizedFeedResponse listFollowing(UUID currentUserId, int page, int size) {
        List<UUID> followedIds = userFollowRepository.findAllFollowedIdsByFollowerId(currentUserId);
        Set<UUID> blockedIds = userBlockRepository.findRelatedUserIds(currentUserId);
        followedIds = followedIds.stream().filter(id -> !blockedIds.contains(id)).toList();
        if (followedIds.isEmpty()) {
            return emptyResponse(page, size, FeedType.FOLLOWING);
        }

        Page<Post> feed = postRepository.findByAuthorUserIdInOrderByCreatedAtDesc(
                followedIds,
                PageRequest.of(page, size)
        );
        Set<UUID> followedAuthorIds = new HashSet<>(followedIds);
        List<FeedPostResponse> items = toFeedResponses(feed.getContent(), currentUserId, followedAuthorIds, null);
        return new PersonalizedFeedResponse(
                items,
                feed.getNumber(),
                feed.getSize(),
                feed.getTotalElements(),
                feed.getTotalPages(),
                feed.isLast(),
                FeedType.FOLLOWING
        );
    }

    private PersonalizedFeedResponse listForYou(UUID currentUserId, int page, int size) {
        List<UUID> followedIds = userFollowRepository.findAllFollowedIdsByFollowerId(currentUserId);
        Set<UUID> followedAuthorIds = new HashSet<>(followedIds);
        Set<String> viewerSkills = new LinkedHashSet<>();
        Set<String> viewerTechnologies = new LinkedHashSet<>();
        buildViewerInterests(currentUserId, followedIds, viewerSkills, viewerTechnologies);

        List<Post> candidates = excludeBlockedAuthors(postRepository.findRecentPosts(FOR_YOU_CANDIDATE_LIMIT), currentUserId);
        if (candidates.isEmpty()) {
            return emptyResponse(page, size, FeedType.FOR_YOU);
        }

        List<UUID> postIds = candidates.stream().map(Post::getId).toList();
        List<UUID> authorIds = candidates.stream().map(Post::getAuthorUserId).distinct().toList();
        Map<UUID, Long> likeCounts = postLikeRepository.countByPostIds(postIds);
        Map<UUID, Long> commentCounts = commentRepository.countByPostIds(postIds);
        Map<UUID, MentorProfile> authorProfiles = mentorProfileRepository.findByUserIdIn(authorIds).stream()
                .collect(Collectors.toMap(MentorProfile::getUserId, Function.identity(), (left, right) -> left));

        Instant now = Instant.now();
        List<ScoredPost> scoredPosts = candidates.stream()
                .map(post -> new ScoredPost(
                        post,
                        FeedScoringService.score(
                                post,
                                followedAuthorIds,
                                viewerSkills,
                                viewerTechnologies,
                                authorProfiles,
                                likeCounts,
                                commentCounts,
                                now
                        )
                ))
                .sorted(Comparator
                        .comparingLong((ScoredPost item) -> item.score())
                        .reversed()
                        .thenComparing(item -> item.post().getCreatedAt(), Comparator.reverseOrder()))
                .toList();

        int fromIndex = Math.min(page * size, scoredPosts.size());
        int toIndex = Math.min(fromIndex + size, scoredPosts.size());
        List<ScoredPost> pageItems = scoredPosts.subList(fromIndex, toIndex);
        List<Post> posts = pageItems.stream().map(ScoredPost::post).toList();
        Map<UUID, Long> scoreByPostId = pageItems.stream()
                .collect(Collectors.toMap(item -> item.post().getId(), ScoredPost::score));

        List<FeedPostResponse> items = toFeedResponses(posts, currentUserId, followedAuthorIds, scoreByPostId);
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) scoredPosts.size() / size);
        return new PersonalizedFeedResponse(
                items,
                page,
                size,
                scoredPosts.size(),
                totalPages,
                toIndex >= scoredPosts.size(),
                FeedType.FOR_YOU
        );
    }

    private void buildViewerInterests(
            UUID currentUserId,
            List<UUID> followedIds,
            Set<String> viewerSkills,
            Set<String> viewerTechnologies
    ) {
        List<UUID> interestUserIds = new ArrayList<>();
        interestUserIds.add(currentUserId);
        interestUserIds.addAll(followedIds);

        mentorProfileRepository.findByUserIdIn(interestUserIds).forEach(profile -> {
            viewerSkills.addAll(normalizeTags(profile.getSkills()));
            viewerTechnologies.addAll(normalizeTags(profile.getTechnologies()));
        });
    }

    private List<FeedPostResponse> toFeedResponses(
            List<Post> posts,
            UUID currentUserId,
            Set<UUID> followedAuthorIds,
            Map<UUID, Long> scoreByPostId
    ) {
        if (posts.isEmpty()) {
            return List.of();
        }

        List<UUID> postIds = posts.stream().map(Post::getId).toList();
        Map<UUID, Long> likeCounts = postLikeRepository.countByPostIds(postIds);
        Set<UUID> likedPostIds = postLikeRepository.findLikedPostIds(currentUserId, postIds);
        Map<UUID, Long> commentCounts = commentRepository.countByPostIds(postIds);

        return posts.stream()
                .map(post -> {
                    PostResponse response = PostResponse.from(
                            post,
                            likeCounts.getOrDefault(post.getId(), 0L),
                            likedPostIds.contains(post.getId()),
                            commentCounts.getOrDefault(post.getId(), 0L)
                    );
                    Long score = scoreByPostId != null ? scoreByPostId.get(post.getId()) : null;
                    return FeedPostResponse.from(
                            response,
                            followedAuthorIds.contains(post.getAuthorUserId()),
                            score
                    );
                })
                .toList();
    }

    private List<Post> excludeBlockedAuthors(List<Post> posts, UUID currentUserId) {
        Set<UUID> blockedIds = userBlockRepository.findRelatedUserIds(currentUserId);
        if (blockedIds.isEmpty()) {
            return posts;
        }
        return posts.stream()
                .filter(post -> !blockedIds.contains(post.getAuthorUserId()))
                .toList();
    }

    private PersonalizedFeedResponse emptyResponse(int page, int size, FeedType type) {
        return new PersonalizedFeedResponse(List.of(), page, size, 0, 0, true, type);
    }

    private static Set<String> normalizeTags(Set<String> tags) {
        Set<String> normalized = new LinkedHashSet<>();
        if (tags == null) {
            return normalized;
        }
        for (String tag : tags) {
            if (tag != null && !tag.isBlank()) {
                normalized.add(tag.trim().toLowerCase());
            }
        }
        return normalized;
    }

    private record ScoredPost(Post post, long score) {
    }
}
