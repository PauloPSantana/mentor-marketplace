"use client";

import Link from "next/link";
import { formatRelativeTime } from "@/lib/feed";
import { markNotificationRead, type Notification } from "@/lib/notifications";

type NotificationListProps = {
  items: Notification[];
  onRead: (id: string) => void;
};

export function NotificationList({ items, onRead }: NotificationListProps) {
  if (items.length === 0) {
    return <p className="feed-empty">Nenhuma notificação por enquanto.</p>;
  }

  return (
    <ul className="notification-list">
      {items.map((item) => (
        <li key={item.id} className={`notification-item${item.read ? "" : " unread"}`}>
          <div className="notification-content">
            <p className="notification-message">{item.message}</p>
            <p className="post-meta">{formatRelativeTime(item.createdAt)}</p>
          </div>
          <div className="notification-actions">
            {item.postId ? (
              <Link className="text-btn" href={`/feed#post-${item.postId}`}>
                Ver publicação
              </Link>
            ) : item.type === "USER_FOLLOWED" ? (
              <Link className="text-btn" href="/mentorias">
                Ver mentores
              </Link>
            ) : null}
            {!item.read ? (
              <button className="text-btn" type="button" onClick={() => void handleRead(item.id, onRead)}>
                Marcar como lida
              </button>
            ) : null}
          </div>
        </li>
      ))}
    </ul>
  );
}

async function handleRead(id: string, onRead: (id: string) => void) {
  await markNotificationRead(id);
  onRead(id);
}
