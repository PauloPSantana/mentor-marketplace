package br.com.mentorhub.identity.application;

import java.util.UUID;

public record MentorUserRegisteredEvent(
        UUID userId,
        String linkedinUrl,
        String photoUrl
) {
    public MentorUserRegisteredEvent(UUID userId) {
        this(userId, null, null);
    }
}
