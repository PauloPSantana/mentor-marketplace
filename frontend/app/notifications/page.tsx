"use client";

import { useCallback, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { NotificationList } from "@/components/notifications/NotificationList";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  listNotifications,
  markAllNotificationsRead,
  type Notification
} from "@/lib/notifications";

export default function NotificationsPage() {
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const [items, setItems] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [unreadCount, setUnreadCount] = useState(0);

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const feed = await listNotifications(0, 30);
      setItems(feed.items);
      setUnreadCount(feed.unreadCount);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível carregar as notificações."));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const user = getStoredUser();
    if (!user) {
      router.replace("/login");
      return;
    }
    setReady(true);
  }, [router]);

  useEffect(() => {
    if (ready) {
      void loadNotifications();
    }
  }, [ready, loadNotifications]);

  async function onMarkAllRead() {
    try {
      await markAllNotificationsRead();
      setItems((current) => current.map((item) => ({ ...item, read: true })));
      setUnreadCount(0);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível marcar todas como lidas."));
    }
  }

  function onRead(id: string) {
    setItems((current) =>
      current.map((item) => (item.id === id ? { ...item, read: true } : item))
    );
    setUnreadCount((count) => Math.max(0, count - 1));
  }

  if (!ready) {
    return <main className="container feed-page">Carregando...</main>;
  }

  return (
    <main className="container feed-page">
      <div className="notifications-header">
        <div>
          <h1 className="help-heading">
            Notificações
            <HelpTooltip
              text="Aqui chegam pedidos, aceites, comentários e outras atualizações da sua conta."
              href="/ajuda/notificacoes"
              label="Ajuda sobre notificações"
            />
          </h1>
          <p className="feed-subtitle">
            {unreadCount > 0
              ? `${unreadCount} ${unreadCount === 1 ? "não lida" : "não lidas"}`
              : "Tudo em dia"}
          </p>
        </div>
        {unreadCount > 0 ? (
          <button className="btn secondary" type="button" onClick={() => void onMarkAllRead()}>
            Marcar todas como lidas
          </button>
        ) : null}
      </div>
      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando notificações...</p> : null}
      {!loading ? <NotificationList items={items} onRead={onRead} /> : null}
    </main>
  );
}
