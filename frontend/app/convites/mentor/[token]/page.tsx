"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { apiErrorMessage } from "@/lib/api";
import { dashboardPath, setAuthSession } from "@/lib/auth";
import { acceptMentorInvitation, getPublicInvitation, type PublicMentorInvitation } from "@/lib/institutions";
import { isPasswordValid, validatePassword } from "@/lib/linkedin";

export default function AcceptMentorInvitePage() {
  const router = useRouter();
  const params = useParams<{ token: string }>();
  const token = params.token;
  const [invitation, setInvitation] = useState<PublicMentorInvitation | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const passwordChecks = useMemo(
    () => validatePassword(password, confirmPassword),
    [password, confirmPassword]
  );

  useEffect(() => {
    if (!token) {
      setError("Convite inválido.");
      return;
    }
    getPublicInvitation(token)
      .then(setInvitation)
      .catch((err) => setError(apiErrorMessage(err, "Convite inválido ou expirado.")));
  }, [token]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    if (!token || !isPasswordValid(passwordChecks)) {
      setError("A senha precisa ter 8+ caracteres, letras e números, e a confirmação deve coincidir.");
      return;
    }
    setLoading(true);
    try {
      const data = await acceptMentorInvitation(token, { password, confirmPassword });
      setAuthSession(data.accessToken, data.user);
      router.push(dashboardPath(data.user.role));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível aceitar o convite."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 520 }}>
      <h1 className="help-heading">
        Você foi convidado!
        <HelpTooltip
          text="Defina sua senha para criar a conta de mentor. Nome e e-mail vêm do convite e não podem ser alterados aqui."
          href="/ajuda/primeiros-passos"
          label="Ajuda sobre convite de mentor"
        />
      </h1>
      {invitation ? (
        <>
          <p style={{ marginBottom: "0.5rem" }}>{invitation.institutionName}</p>
          <p className="post-meta" style={{ marginBottom: "1.5rem" }}>
            Complete seu cadastro. Este convite é pessoal e tem prazo de validade.
          </p>
          <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
            <label>
              Nome
              <input className="input" value={invitation.name} readOnly />
            </label>
            <label>
              E-mail
              <input className="input" type="email" value={invitation.email} readOnly />
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
            {error ? <p className="error">{error}</p> : null}
            <button className="btn" type="submit" disabled={loading || !isPasswordValid(passwordChecks)}>
              {loading ? "Criando conta..." : "ACEITAR E CRIAR CONTA"}
            </button>
          </form>
        </>
      ) : (
        <>
          {error ? <p className="error">{error}</p> : <p>Carregando convite...</p>}
          <p style={{ marginTop: "1rem" }}>
            Já tem conta? <Link href="/login">Entrar</Link>
          </p>
        </>
      )}
    </main>
  );
}
