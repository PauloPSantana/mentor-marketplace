package br.com.mentorhub.social.api.dto;

public record FollowStatusResponse(
        boolean following,
        long followerCount,
        long followingCount
) {
}
