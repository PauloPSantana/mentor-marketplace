package br.com.mentorhub.notifications.api.dto;

import java.util.List;

public record NotificationFeedResponse(
        List<NotificationResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last,
        long unreadCount
) {
}
