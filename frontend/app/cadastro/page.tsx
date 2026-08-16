"use client";

import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";

type UserResponse = {
  id: string;
  name: string;
  email: string;
  role: "MENTOR" | "MENTEE" | "ADMIN";
};

export default function CadastroPage() {
  const router = useRouter();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);

    const form = new FormData(event.currentTarget);
    try {
      await api<UserResponse>("/api/v1/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name: form.get("name"),
          email: form.get("email"),
          password: form.get("password"),
          role: form.get("role")
        })
      });
      router.push("/login");
    } catch {
      setError("Não foi possível concluir o cadastro. Verifique os dados ou se o email já existe.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 520 }}>
      <h1>Criar conta</h1>
      <p style={{ marginBottom: "1.5rem" }}>Cadastre-se como mentor ou mentorado.</p>
      <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
        <label>
          Nome
          <input className="input" name="name" required maxLength={120} />
        </label>
        <label>
          Email
          <input className="input" name="email" type="email" required />
        </label>
        <label>
          Senha
          <input className="input" name="password" type="password" minLength={8} required />
        </label>
        <label>
          Quero ser
          <select className="input" name="role" defaultValue="MENTEE">
            <option value="MENTEE">Mentorado</option>
            <option value="MENTOR">Mentor</option>
          </select>
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="btn" type="submit" disabled={loading}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>
      <p style={{ marginTop: "1rem" }}>
        Já tem conta? <Link href="/login">Entrar</Link>
      </p>
    </main>
  );
}
