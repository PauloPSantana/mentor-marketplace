"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { dashboardPath, setAuthSession } from "@/lib/auth";
import { HelpTooltip } from "@/components/help/HelpTooltip";

type AuthResponse = {
  accessToken: string;
  user: { id: string; name: string; email: string; role: string };
};

export default function LoginPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    const form = new FormData(event.currentTarget);
    try {
      const data = await api<AuthResponse>("/api/v1/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email: form.get("email"),
          password: form.get("password")
        })
      });
      setAuthSession(data.accessToken, data.user);
      router.push(dashboardPath(data.user.role));
    } catch {
      setError("Credenciais inválidas. Verifique email e senha.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 480 }}>
      <h1 className="help-heading">
        Entrar
        <HelpTooltip
          text="Use o email e a senha do cadastro. Depois do login, você vai para o painel de mentor ou mentorado."
          href="/ajuda/primeiros-passos"
          label="Ajuda sobre login"
        />
      </h1>
        <p style={{ marginBottom: "1.5rem" }}>Acesse sua conta de mentor, mentorado ou instituição.</p>
      <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
        <label>
          Email
          <input className="input" name="email" type="email" required />
        </label>
        <label>
          Senha
          <input className="input" name="password" type="password" minLength={8} required />
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="btn" type="submit" disabled={loading}>
          {loading ? "Entrando..." : "Entrar"}
        </button>
      </form>
      <p style={{ marginTop: "1rem" }}>
        Não tem conta? <Link href="/cadastro">Cadastre-se</Link>
      </p>
    </main>
  );
}
