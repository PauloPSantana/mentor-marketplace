"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import {
  AUTH_EVENT,
  clearAuthSession,
  dashboardPath,
  getStoredUser,
  roleLabel,
  type StoredUser
} from "@/lib/auth";

export function AppHeader() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [ready, setReady] = useState(false);

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

  function logout() {
    clearAuthSession();
    setUser(null);
    router.push("/");
  }

  return (
    <header style={{ borderBottom: "1px solid var(--line)", background: "rgba(255,255,255,0.8)" }}>
      <div
        className="container"
        style={{ display: "flex", justifyContent: "space-between", alignItems: "center", padding: "1rem 0", gap: "1rem", flexWrap: "wrap" }}
      >
        <Link href="/" style={{ fontWeight: 800, fontSize: "1.15rem" }}>
          MentorHub AI
        </Link>
        <nav style={{ display: "flex", gap: "1rem", alignItems: "center", flexWrap: "wrap" }}>
          <Link href="/mentorias">Mentorias</Link>
          {!ready ? null : user ? (
            <>
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
