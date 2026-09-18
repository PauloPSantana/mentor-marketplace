"use client";

import { FormEvent, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { assignMentorship, type MentorshipRelationship } from "@/lib/mentorships";

const PROGRAMS = [
  "Mentoria de Tecnologia",
  "Mentoria de Carreira",
  "Mentoria de Liderança",
  "Mentoria de Produto"
];

export type AssignableMentor = {
  mentorProfileId: string;
  name: string;
  active: boolean;
};

type AssignMentorshipFormProps = {
  mentors?: AssignableMentor[];
  onAssigned: (mentorship: MentorshipRelationship) => void;
};

export function AssignMentorshipForm({ mentors, onAssigned }: AssignMentorshipFormProps) {
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const showMentorSelect = Array.isArray(mentors);
  const activeMentors = (mentors ?? []).filter((mentor) => mentor.active);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formEl = event.currentTarget;
    setError(null);
    setMessage(null);
    if (saving) return;
    if (showMentorSelect && activeMentors.length === 0) {
      setError("Convide e ative um mentor antes de vincular mentorados.");
      return;
    }
    setSaving(true);
    const form = new FormData(formEl);
    try {
      const created = await assignMentorship({
        menteeEmail: String(form.get("menteeEmail") ?? ""),
        program: String(form.get("program") ?? ""),
        mentorProfileId: showMentorSelect ? String(form.get("mentorProfileId") ?? "") || undefined : undefined
      });
      formEl.reset();
      onAssigned(created);
      setMessage(`Mentorado vinculado${created.mentee?.name ? `: ${created.mentee.name}` : "."}`);
    } catch (err) {
      setMessage(null);
      setError(apiErrorMessage(err, "Não foi possível vincular o mentorado."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <form className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "0.75rem", maxWidth: 520 }}>
      {showMentorSelect ? (
        <label>
          Mentor
          <select className="input" name="mentorProfileId" required disabled={activeMentors.length === 0}>
            {activeMentors.length === 0 ? (
              <option value="">Nenhum mentor ativo</option>
            ) : (
              activeMentors.map((mentor) => (
                <option key={mentor.mentorProfileId} value={mentor.mentorProfileId}>
                  {mentor.name}
                </option>
              ))
            )}
          </select>
        </label>
      ) : null}
      <label>
        E-mail do mentorado
        <input
          className="input"
          name="menteeEmail"
          type="email"
          required
          maxLength={180}
          placeholder="mentorado@email.com"
        />
      </label>
      <label>
        Programa
        <select className="input" name="program" defaultValue={PROGRAMS[0]}>
          {PROGRAMS.map((program) => (
            <option key={program} value={program}>{program}</option>
          ))}
        </select>
      </label>
      <p className="post-meta">O mentorado precisa já ter uma conta na plataforma.</p>
      {error ? <p className="error">{error}</p> : null}
      {!error && message ? <p style={{ color: "var(--accent)" }}>{message}</p> : null}
      <button className="btn" type="submit" disabled={saving}>
        {saving ? "Vinculando..." : "Vincular mentorado"}
      </button>
    </form>
  );
}
