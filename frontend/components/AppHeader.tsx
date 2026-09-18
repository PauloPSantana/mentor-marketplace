"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { agendaPath } from "@/lib/dashboard";
import {
  AUTH_EVENT,
  clearAuthSession,
  dashboardPath,
  getStoredUser,
  roleLabel,
  type StoredUser
} from "@/lib/auth";
import { getUnreadNotificationCount } from "@/lib/notifications";
import { ThemeToggle } from "@/components/ThemeToggle";

export function AppHeader() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [ready, setReady] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const sync = () => {
      setUser(getStoredUser());
      setReady(true);
    };
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    window.addEventListener("storage", sync);
    return () => {
      window.removeEventListener(AUTH_EVENT, sync);
      window.removeEventListener("storage", sync);
    };
  }, []);

  useEffect(() => {
    if (!user) {
      setUnreadCount(0);
      return;
    }

    let cancelled = false;

    async function loadUnreadCount() {
      try {
        const response = await getUnreadNotificationCount();
        if (!cancelled) {
          setUnreadCount(response.count);
        }
      } catch {
        if (!cancelled) {
          setUnreadCount(0);
        }
      }
    }

    void loadUnreadCount();
    const intervalId = window.setInterval(() => {
      void loadUnreadCount();
    }, 60_000);

    return () => {
      cancelled = true;
      window.clearInterval(intervalId);
    };
  }, [user]);

  function logout() {
    clearAuthSession();
    setUser(null);
    router.push("/");
  }

  return (
    <header className="site-header">
      <div
        className="container"
        style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1rem 0", gap: "1rem", flexWrap: "wrap" }}
      >
        <Link href="/" style={{ fontWeight: 800, fontSize: "1.15rem" }}>
          MentorHub AI
        </Link>
        <nav style={{ display: "flex", gap: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <ThemeToggle />
          <Link href="/ajuda">Ajuda</Link>
          {user ? <Link href="/feed">Feed</Link> : null}
          {user ? <Link href={agendaPath(user.role)}>Agenda</Link> : null}
          {user ? <Link href="/grupos">Grupos</Link> : null}
          {!ready ? null : user ? (
            <>
              <Link href="/notifications" className="notification-link">
                🔔 Notificações
                {unreadCount > 0 ? <span className="notification-badge">{unreadCount}</span> : null}
              </Link>
              <Link href={dashboardPath(user.role)}>Meu painel</Link>
              <div style={{ display: "flex", alignItems: "center", gap: "0.65rem" }}>
                <span style={{ fontWeight: 600 }}>{user.name}</span>
                <span className="role-badge">{roleLabel(user.role)}</span>
              </div>
              <button className="btn secondary" type="button" onClick={logout} style={{ padding: "0.55rem 0.9rem" }}>
                Sair
              </button>
            </>
          ) : (
            <>
              <Link href="/login">Entrar</Link>
              <Link href="/cadastro" className="btn">
                Criar conta
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
