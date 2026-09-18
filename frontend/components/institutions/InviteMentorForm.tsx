"use client";

import { FormEvent, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  inviteMentor,
  type InstitutionInvitation
} from "@/lib/institutions";
import { MENTORSHIP_PROGRAMS } from "@/lib/programs";

type InviteMentorFormProps = {
  institutionName?: string;
  onCreated: (invitation: InstitutionInvitation) => void;
};

export function InviteMentorForm({ institutionName, onCreated }: InviteMentorFormProps) {
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [lastInvite, setLastInvite] = useState<InstitutionInvitation | null>(null);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formEl = event.currentTarget;
    setError(null);
    setMessage(null);
    if (saving) return;
    setSaving(true);
    const form = new FormData(formEl);
    try {
      const created = await inviteMentor({
        name: String(form.get("name") ?? ""),
        email: String(form.get("email") ?? ""),
        program: String(form.get("program") ?? "") || undefined
      });
      formEl.reset();
      setLastInvite(created);
      onCreated(created);
      setMessage(
        created.emailSent
          ? `Convite enviado para ${created.email}`
          : `Convite criado para ${created.email}. Copie o link e envie ao mentor.`
      );
    } catch (err) {
      setMessage(null);
      setError(apiErrorMessage(err, "Não foi possível enviar o convite."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "0.75rem", maxWidth: 520 }}>
      <p className="post-meta">
        O mentor recebe o convite, cria a senha e fica vinculado a {institutionName ?? "sua instituição"}.
      </p>
      <label>
        Nome
        <input className="input" name="name" required maxLength={120} placeholder="Carlos Silva" />
      </label>
      <label>
        E-mail
        <input className="input" name="email" type="email" required maxLength={180} placeholder="carlos@email.com" />
      </label>
      <label>
        Programa
        <select className="input" name="program" defaultValue={MENTORSHIP_PROGRAMS[0]}>
          {MENTORSHIP_PROGRAMS.map((program) => (
            <option key={program} value={program}>{program}</option>
          ))}
        </select>
      </label>
      {error ? <p className="error">{error}</p> : null}
      {!error && message ? <p style={{ color: "var(--accent)" }}>{message}</p> : null}
      {lastInvite && !lastInvite.emailSent ? (
        <p className="post-meta">
          Link: <a href={lastInvite.inviteUrl}>{lastInvite.inviteUrl}</a>{" "}
          <button className="text-btn" type="button" onClick={() => void navigator.clipboard.writeText(lastInvite.inviteUrl)}>
            Copiar
          </button>
        </p>
      ) : null}
      <button className="btn" type="submit" disabled={saving}>
        {saving ? "Enviando..." : "Enviar convite"}
      </button>
    </form>
  );
}
