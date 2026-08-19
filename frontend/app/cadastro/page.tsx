"use client";

import Link from "next/link";
import { FormEvent, Suspense, useEffect, useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { api, apiErrorMessage } from "@/lib/api";
import { isPasswordValid, validatePassword } from "@/lib/linkedin";

type UserResponse = {
  id: string;
  name: string;
  email: string;
  role: "MENTOR" | "MENTEE" | "ADMIN";
};

type UserRole = "MENTOR" | "MENTEE";

function CadastroForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const initialRole: UserRole = searchParams.get("papel") === "mentor" ? "MENTOR" : "MENTEE";
  const [role, setRole] = useState<UserRole>(initialRole);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  useEffect(() => {
    setRole(searchParams.get("papel") === "mentor" ? "MENTOR" : "MENTEE");
  }, [searchParams]);

  const passwordChecks = useMemo(
    () => validatePassword(password, confirmPassword),
    [password, confirmPassword]
  );

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    if (!isPasswordValid(passwordChecks)) {
      setError("A senha precisa ter 8+ caracteres, letras e números, e a confirmação deve coincidir.");
      return;
    }
    setLoading(true);

    const form = new FormData(event.currentTarget);
    try {
      await api<UserResponse>("/api/v1/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name: form.get("name"),
          email: form.get("email"),
          password,
          confirmPassword,
          role
        })
      });
      router.push("/login");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível concluir o cadastro. Verifique os dados ou se o email já existe."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 520 }}>
      <h1 className="help-heading">
        {role === "MENTOR" ? "Criar conta de mentor" : "Criar conta"}
        <HelpTooltip
          text="Escolha Mentorado para buscar mentores ou Mentor para oferecer sessões. A senha precisa ter 8+ caracteres, com letras e números."
          href="/ajuda/primeiros-passos"
          label="Ajuda sobre cadastro"
        />
      </h1>
      <p style={{ marginBottom: "1.5rem" }}>
        {role === "MENTOR"
          ? "Cadastre-se para oferecer mentorias e aparecer no marketplace."
          : "Cadastre-se como mentor ou mentorado."}
      </p>
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
          <input
            className="input"
            name="password"
            type="password"
            minLength={8}
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
        </label>
        <label>
          Confirmar senha
          <input
            className="input"
            name="confirmPassword"
            type="password"
            minLength={8}
            required
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
          />
        </label>
        <ul className="password-checks">
          <li className={passwordChecks.length ? "ok" : ""}>Pelo menos 8 caracteres</li>
          <li className={passwordChecks.letter ? "ok" : ""}>Contém letras</li>
          <li className={passwordChecks.digit ? "ok" : ""}>Contém números</li>
          <li className={passwordChecks.match ? "ok" : ""}>As senhas coincidem</li>
        </ul>
        <label>
          Quero ser
          <select
            className="input"
            name="role"
            value={role}
            onChange={(event) => setRole(event.target.value === "MENTOR" ? "MENTOR" : "MENTEE")}
          >
            <option value="MENTEE">Mentorado</option>
            <option value="MENTOR">Mentor</option>
          </select>
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="btn" type="submit" disabled={loading || !isPasswordValid(passwordChecks)}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>
      <p style={{ marginTop: "1rem" }}>
        Já tem conta? <Link href="/login">Entrar</Link>
      </p>
    </main>
  );
}

export default function CadastroPage() {
  return (
    <Suspense fallback={<main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>}>
      <CadastroForm />
    </Suspense>
  );
}
