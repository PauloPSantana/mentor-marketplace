package br.com.mentorhub.social.application;

import java.util.UUID;

public record UserFollowedEvent(UUID followedUserId, UUID followerUserId) {
}
