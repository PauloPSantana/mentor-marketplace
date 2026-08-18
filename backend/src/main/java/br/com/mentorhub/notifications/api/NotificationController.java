package br.com.mentorhub.notifications.api;

import br.com.mentorhub.notifications.api.dto.NotificationFeedResponse;
import br.com.mentorhub.notifications.api.dto.UnreadCountResponse;
import br.com.mentorhub.notifications.application.GetUnreadNotificationCountService;
import br.com.mentorhub.notifications.application.ListNotificationsService;
import br.com.mentorhub.notifications.application.MarkAllNotificationsReadService;
import br.com.mentorhub.notifications.application.MarkNotificationReadService;
import br.com.mentorhub.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final ListNotificationsService listNotificationsService;
    private final GetUnreadNotificationCountService getUnreadNotificationCountService;
    private final MarkNotificationReadService markNotificationReadService;
    private final MarkAllNotificationsReadService markAllNotificationsReadService;

    public NotificationController(
            ListNotificationsService listNotificationsService,
            GetUnreadNotificationCountService getUnreadNotificationCountService,
            MarkNotificationReadService markNotificationReadService,
            MarkAllNotificationsReadService markAllNotificationsReadService
    ) {
        this.listNotificationsService = listNotificationsService;
        this.getUnreadNotificationCountService = getUnreadNotificationCountService;
        this.markNotificationReadService = markNotificationReadService;
        this.markAllNotificationsReadService = markAllNotificationsReadService;
    }

    @GetMapping
    public ResponseEntity<NotificationFeedResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listNotificationsService.execute(SecurityUtils.requireCurrentUserId(), page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        return ResponseEntity.ok(getUnreadNotificationCountService.execute(SecurityUtils.requireCurrentUserId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable UUID id) {
        markNotificationReadService.execute(id, SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        markAllNotificationsReadService.execute(SecurityUtils.requireCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
