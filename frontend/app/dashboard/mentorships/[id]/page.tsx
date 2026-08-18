"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AgendaList } from "@/components/mentorships/AgendaList";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  cancelMentorship,
  completeMentorship,
  createMentorshipSession,
  formatSessionDate,
  getMentorshipRelationship,
  listMentorshipSessions,
  mentorshipStatusLabel,
  type MentorshipRelationship,
  type MentorshipSession
} from "@/lib/mentorships";

export default function MentorshipDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const currentUser = getStoredUser();
  const userId = currentUser?.id;
  const [mentorship, setMentorship] = useState<MentorshipRelationship | null>(null);
  const [sessions, setSessions] = useState<MentorshipSession[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [scheduledAt, setScheduledAt] = useState("");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [meetingUrl, setMeetingUrl] = useState("");
  const [notes, setNotes] = useState("");

  const isMentor = currentUser?.id === mentorship?.mentor.id || currentUser?.role === "ADMIN";
  const isParticipant = currentUser?.id === mentorship?.mentor.id || currentUser?.id === mentorship?.mentee.id;

  useEffect(() => {
    if (!userId) {
      router.replace("/login");
      return;
    }
    Promise.all([getMentorshipRelationship(params.id), listMentorshipSessions(params.id)])
      .then(([relationship, items]) => {
        setMentorship(relationship);
        setSessions(items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar a mentoria.")))
      .finally(() => setLoading(false));
  }, [params.id, router, userId]);

  const other = useMemo(() => {
    if (!mentorship || !currentUser) return null;
    return currentUser.id === mentorship.mentor.id ? mentorship.mentee : mentorship.mentor;
  }, [mentorship, currentUser]);

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!mentorship) return;
    setError(null);
    setSaving(true);
    try {
      const created = await createMentorshipSession(mentorship.id, {
        scheduledAt: new Date(scheduledAt).toISOString(),
        durationMinutes,
        meetingUrl,
        notes
      });
      setSessions((current) => [...current, created].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)));
      setScheduledAt("");
      setMeetingUrl("");
      setNotes("");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível agendar a sessão."));
    } finally {
      setSaving(false);
    }
  }

  async function onCompleteMentorship() {
    if (!mentorship) return;
    setError(null);
    try {
      setMentorship(await completeMentorship(mentorship.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível concluir a mentoria."));
    }
  }

  async function onCancelMentorship() {
    if (!mentorship) return;
    setError(null);
    try {
      setMentorship(await cancelMentorship(mentorship.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível cancelar a mentoria."));
    }
  }

  if (loading) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  if (!mentorship) {
    return (
      <main className="container" style={{ padding: "3rem 0" }}>
        <p className="error">{error ?? "Mentoria não encontrada."}</p>
      </main>
    );
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 760 }}>
      <p className="post-meta">
        <Link href="/agenda">Agenda</Link>
      </p>
      <h1>{mentorship.serviceName}</h1>
      <p>
        Com {other?.name}.{" "}
        <span className={`status-badge ${mentorship.status.toLowerCase()}`}>
          {mentorshipStatusLabel(mentorship.status)}
        </span>
      </p>
      {mentorship.nextSession ? (
        <p className="post-meta">Próxima sessão: {formatSessionDate(mentorship.nextSession.scheduledAt)}</p>
      ) : null}
      {error ? <p className="error">{error}</p> : null}

      {mentorship.status === "ACTIVE" && isParticipant ? (
        <form className="card-form" onSubmit={onCreate} style={{ display: "grid", gap: "0.75rem", margin: "1.5rem 0" }}>
          <h2>Agendar sessão</h2>
          <label>
            Data e horário
            <input className="input" type="datetime-local" required value={scheduledAt} onChange={(event) => setScheduledAt(event.target.value)} />
          </label>
          <label>
            Duração (minutos)
            <input
              className="input"
              type="number"
              min={1}
              max={240}
              value={durationMinutes}
              onChange={(event) => setDurationMinutes(Number(event.target.value))}
            />
          </label>
          <label>
            Link da reunião (opcional)
            <input className="input" value={meetingUrl} onChange={(event) => setMeetingUrl(event.target.value)} />
          </label>
          <label>
            Observações
            <textarea className="input" rows={3} maxLength={2000} value={notes} onChange={(event) => setNotes(event.target.value)} />
          </label>
          <button className="btn" type="submit" disabled={saving}>
            {saving ? "Agendando..." : "Criar sessão"}
          </button>
        </form>
      ) : null}

      <section className="enrollment-section">
        <h2>Sessões</h2>
        <AgendaList items={sessions} viewerRole={isMentor ? "MENTOR" : "MENTEE"} onChange={setSessions} />
      </section>

      {isMentor && (mentorship.status === "ACTIVE" || mentorship.status === "PAUSED") ? (
        <div style={{ display: "flex", gap: "0.75rem", marginTop: "1.5rem" }}>
          <button className="btn" type="button" onClick={() => void onCompleteMentorship()}>
            Concluir mentoria
          </button>
          <button className="btn secondary" type="button" onClick={() => void onCancelMentorship()}>
            Cancelar mentoria
          </button>
        </div>
      ) : null}
    </main>
  );
}
