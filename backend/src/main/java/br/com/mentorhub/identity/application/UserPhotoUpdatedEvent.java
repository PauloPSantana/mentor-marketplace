package br.com.mentorhub.identity.application;

import java.util.UUID;

public record UserPhotoUpdatedEvent(UUID userId, String photoUrl) {
}
