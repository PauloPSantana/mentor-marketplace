package br.com.mentorhub.feed.application;

import br.com.mentorhub.feed.domain.Post;
import br.com.mentorhub.mentors.domain.MentorProfile;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class FeedScoringService {

    static final int FOLLOW_WEIGHT = 100;
    static final int SPECIALTY_WEIGHT = 30;
    static final int TECHNOLOGY_WEIGHT = 20;
    static final int ENGAGEMENT_WEIGHT = 10;
    static final int MAX_RECENCY_WEIGHT = 50;

    private FeedScoringService() {
    }

    static long score(
            Post post,
            Set<UUID> followedAuthorIds,
            Set<String> viewerSkills,
            Set<String> viewerTechnologies,
            Map<UUID, MentorProfile> authorProfiles,
            Map<UUID, Long> likeCounts,
            Map<UUID, Long> commentCounts,
            Instant now
    ) {
        long total = 0;

        if (followedAuthorIds.contains(post.getAuthorUserId())) {
            total += FOLLOW_WEIGHT;
        }

        MentorProfile authorProfile = authorProfiles.get(post.getAuthorUserId());
        if (authorProfile != null) {
            if (hasOverlap(normalizeTags(authorProfile.getSkills()), viewerSkills)) {
                total += SPECIALTY_WEIGHT;
            }
            if (hasOverlap(normalizeTags(authorProfile.getTechnologies()), viewerTechnologies)) {
                total += TECHNOLOGY_WEIGHT;
            }
        }

        long engagement = likeCounts.getOrDefault(post.getId(), 0L)
                + commentCounts.getOrDefault(post.getId(), 0L);
        total += engagement * ENGAGEMENT_WEIGHT;
        total += recencyWeight(post.getCreatedAt(), now);
        return total;
    }

    private static long recencyWeight(Instant createdAt, Instant now) {
        long hoursOld = Math.max(0, Duration.between(createdAt, now).toHours());
        return Math.max(0, MAX_RECENCY_WEIGHT - hoursOld);
    }

    private static boolean hasOverlap(Set<String> left, Set<String> right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }
        for (String value : left) {
            if (right.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> normalizeTags(Set<String> tags) {
        Set<String> normalized = new HashSet<>();
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
}
