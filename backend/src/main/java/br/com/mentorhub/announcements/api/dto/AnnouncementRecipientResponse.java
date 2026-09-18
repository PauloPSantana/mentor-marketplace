package br.com.mentorhub.announcements.api.dto;

import java.util.UUID;

public record AnnouncementRecipientResponse(
        UUID userId,
        String name
) {
}
