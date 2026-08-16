"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { clearAuthSession, getStoredUser, roleLabel, type StoredUser } from "@/lib/auth";

export default function MentoradoDashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);

  useEffect(() => {
    const parsed = getStoredUser();
    if (!parsed) {
      router.replace("/login");
      return;
    }
    if (parsed.role !== "MENTEE") {
      router.replace("/dashboard/mentor");
      return;
    }
    setUser(parsed);
  }, [router]);

  if (!user) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  return (
    <main className="container" style={{ padding: "3rem 0" }}>
      <h1>Dashboard do mentorado</h1>
      <p>
        Olá, {user.name}. <span className="role-badge">{roleLabel(user.role)}</span>
      </p>
      <p style={{ marginTop: "0.75rem" }}>Em breve: contratações, sessões e progresso.</p>
      <div style={{ display: "flex", gap: "0.75rem", marginTop: "1.5rem" }}>
        <Link href="/feed" className="btn">Ir para o feed</Link>
        <Link href="/mentorias" className="btn secondary">Buscar mentorias</Link>
        <button
          className="btn"
          type="button"
          onClick={() => {
            clearAuthSession();
            router.push("/");
          }}
        >
          Sair
        </button>
      </div>
    </main>
  );
}
