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
  | "SESSION_REMINDER"
  | "SESSION_RESCHEDULED"
  | "PAYMENT_PAID"
  | "PAYMENT_FAILED"
  | "REVIEW_RECEIVED"
  | "GROUP_CREATED"
  | "GROUP_MEMBER_ADDED"
  | "GROUP_MEMBER_LEFT"
  | "ANNOUNCEMENT_PUBLISHED"
  | "ANNOUNCEMENT_LIKED"
  | "ANNOUNCEMENT_COMMENTED"
  | "ANNOUNCEMENT_REPLIED"
  | "STUDY_TASK_ASSIGNED"
  | "STUDY_PLAN_COMPLETED";

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
