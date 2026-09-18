package br.com.mentorhub.identity.application;

import java.util.UUID;

public record MentorUserRegisteredEvent(
        UUID userId,
        String linkedinUrl,
        String photoUrl,
        String invitationToken
) {
    public MentorUserRegisteredEvent(UUID userId) {
        this(userId, null, null, null);
    }

    public MentorUserRegisteredEvent(UUID userId, String linkedinUrl, String photoUrl) {
        this(userId, linkedinUrl, photoUrl, null);
    }
}
