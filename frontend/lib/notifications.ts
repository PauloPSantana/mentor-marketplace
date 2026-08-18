import { api } from "@/lib/api";

export type NotificationType =
  | "POST_LIKED"
  | "POST_COMMENTED"
  | "COMMENT_REPLIED"
  | "USER_FOLLOWED"
  | "MENTORSHIP_REQUESTED"
  | "MENTORSHIP_ACCEPTED"
  | "MENTORSHIP_REJECTED"
  | "MENTORSHIP_CANCELLED"
  | "MENTORSHIP_COMPLETED"
  | "SESSION_CREATED"
  | "SESSION_CANCELLED"
  | "SESSION_COMPLETED"
  | "SESSION_NO_SHOW"
  | "SESSION_REMINDER";

export type Notification = {
  id: string;
  type: NotificationType;
  read: boolean;
  createdAt: string;
  actorUserId: string;
  actorName: string;
  postId: string | null;
  commentId: string | null;
  message: string;
};

export type NotificationFeed = {
  items: Notification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  unreadCount: number;
};

export type UnreadCount = {
  count: number;
};

export function listNotifications(page = 0, size = 20): Promise<NotificationFeed> {
  return api<NotificationFeed>(`/api/v1/notifications?page=${page}&size=${size}`);
}

export function getUnreadNotificationCount(): Promise<UnreadCount> {
  return api<UnreadCount>("/api/v1/notifications/unread-count");
}

export function markNotificationRead(id: string): Promise<void> {
  return api<void>(`/api/v1/notifications/${id}/read`, { method: "PATCH" });
}

export function markAllNotificationsRead(): Promise<void> {
  return api<void>("/api/v1/notifications/read-all", { method: "PATCH" });
}
