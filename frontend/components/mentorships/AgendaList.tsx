"use client";

import { FormEvent, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  cancelSession,
  completeSession,
  formatSessionDay,
  formatSessionTime,
  markSessionNoShow,
  meetingProviderLabel,
  rescheduleSession,
  sessionStatusLabel,
  type MentorshipSession
} from "@/lib/mentorships";

type AgendaListProps = {
  items: MentorshipSession[];
  viewerRole?: "MENTOR" | "MENTEE";
  onChange?: (items: MentorshipSession[]) => void;
};

function toDateTimeLocal(value: string): string {
  const date = new Date(value);
  const pad = (part: number) => String(part).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function joinHref(item: MentorshipSession, viewerRole?: "MENTOR" | "MENTEE"): string | null {
  if (viewerRole === "MENTOR" && item.hostUrl) {
    return item.hostUrl;
  }
  return item.meetingUrl;
}

export function AgendaList({ items, viewerRole, onChange }: AgendaListProps) {
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [rescheduleId, setRescheduleId] = useState<string | null>(null);
  const [completeId, setCompleteId] = useState<string | null>(null);
  const [scheduledAt, setScheduledAt] = useState("");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [notes, setNotes] = useState("");

  if (items.length === 0) {
    return <p className="feed-empty">Nenhuma sessão neste período.</p>;
  }

  async function run(id: string, action: (id: string) => Promise<MentorshipSession>) {
    setError(null);
    setBusyId(id);
    try {
      const updated = await action(id);
      onChange?.(items.map((item) => (item.id === id ? updated : item)));
      setRescheduleId(null);
      setCompleteId(null);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar a sessão."));
    } finally {
      setBusyId(null);
    }
  }

  function startReschedule(item: MentorshipSession) {
    setRescheduleId(item.id);
    setCompleteId(null);
    setScheduledAt(toDateTimeLocal(item.scheduledAt));
    setDurationMinutes(item.durationMinutes);
  }

  async function onReschedule(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    await run(id, () =>
      rescheduleSession(id, {
        scheduledAt: new Date(scheduledAt).toISOString(),
        durationMinutes
      })
    );
  }

  async function onComplete(event: FormEvent<HTMLFormElement>, id: string) {
    event.preventDefault();
    await run(id, () => completeSession(id, notes));
  }

  return (
    <div>
      {error ? <p className="error">{error}</p> : null}
      <ul className="enrollment-list">
        {items.map((item) => {
          const href = joinHref(item, viewerRole);
          return (
            <li key={item.id} className="enrollment-item">
              <div>
                <p className="enrollment-title">{item.participant.name}</p>
                <p className="post-meta">
                  Data: {formatSessionDay(item.scheduledAt)} · Horário: {formatSessionTime(item.scheduledAt)} · Duração: {item.durationMinutes} min
                </p>
                {item.meetingProvider === "GOOGLE_MEET" ? (
                  <p className="post-meta">
                    Google Agenda: {item.googleCalendarCreated ? "criado" : "pendente"}
                    {" · "}
                    Google Meet: {item.googleMeetCreated ? "criado" : "pendente"}
                  </p>
                ) : item.meetingProvider ? (
                  <p className="post-meta">
                    {meetingProviderLabel(item.meetingProvider)}
                    {item.zoomStatus === "ENDED" ? " encerrada" : item.status === "SCHEDULED" ? " pronta" : ""}
                  </p>
                ) : item.zoomMeeting ? (
                  <p className="post-meta">Reunião Zoom {item.zoomStatus === "ENDED" ? "encerrada" : "pronta"}</p>
                ) : null}
                {item.zoomStatus === "ENDED" && item.status === "SCHEDULED" ? (
                  <p className="post-meta">Reunião encerrada — registre a conclusão.</p>
                ) : null}
                {href && item.status === "SCHEDULED" ? (
                  <p className="post-meta">
                    <a href={href} target="_blank" rel="noreferrer">
                      {viewerRole === "MENTOR" && item.hostUrl ? "Iniciar reunião" : "Entrar na reunião"}
                    </a>
                  </p>
                ) : null}
                {item.notes ? <p className="post-content">{item.notes}</p> : null}
                {rescheduleId === item.id ? (
                  <form className="card-form" onSubmit={(event) => void onReschedule(event, item.id)} style={{ display: "grid", gap: "0.5rem", marginTop: "0.75rem" }}>
                    <label>
                      Nova data e horário
                      <input className="input" type="datetime-local" required value={scheduledAt} onChange={(event) => setScheduledAt(event.target.value)} />
                    </label>
                    <label>
                      Duração (minutos)
                      <input className="input" type="number" min={1} max={240} value={durationMinutes} onChange={(event) => setDurationMinutes(Number(event.target.value))} />
                    </label>
                    <div style={{ display: "flex", gap: "0.5rem" }}>
                      <button className="btn" type="submit" disabled={busyId === item.id}>Salvar</button>
                      <button className="btn secondary" type="button" onClick={() => setRescheduleId(null)}>Fechar</button>
                    </div>
                  </form>
                ) : null}
                {completeId === item.id ? (
                  <form className="card-form" onSubmit={(event) => void onComplete(event, item.id)} style={{ display: "grid", gap: "0.5rem", marginTop: "0.75rem" }}>
                    <p>Sessão concluída</p>
                    <label>
                      Registrar observações
                      <textarea className="input" rows={3} maxLength={2000} value={notes} onChange={(event) => setNotes(event.target.value)} />
                    </label>
                    <div style={{ display: "flex", gap: "0.5rem" }}>
                      <button className="btn" type="submit" disabled={busyId === item.id}>Salvar conclusão</button>
                      <button className="btn secondary" type="button" onClick={() => setCompleteId(null)}>Fechar</button>
                    </div>
                  </form>
                ) : null}
              </div>
              <div className="enrollment-actions">
                <span className={`status-badge ${item.status.toLowerCase()}`}>
                  {sessionStatusLabel(item.status)}
                </span>
                {item.status === "SCHEDULED" && viewerRole === "MENTEE" ? (
                  <>
                    <button className="btn secondary" type="button" disabled={busyId === item.id} onClick={() => startReschedule(item)}>
                      Reagendar
                    </button>
                    <button
                      className="btn secondary"
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => void run(item.id, (id) => cancelSession(id))}
                    >
                      Cancelar
                    </button>
                  </>
                ) : null}
                {item.status === "SCHEDULED" && viewerRole === "MENTOR" ? (
                  <>
                    <button
                      className="btn"
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => {
                        setCompleteId(item.id);
                        setRescheduleId(null);
                        setNotes(item.notes ?? "");
                      }}
                    >
                      Concluir
                    </button>
                    <button className="btn secondary" type="button" disabled={busyId === item.id} onClick={() => startReschedule(item)}>
                      Reagendar
                    </button>
                    <button className="btn secondary" type="button" disabled={busyId === item.id} onClick={() => void run(item.id, markSessionNoShow)}>
                      Ausência
                    </button>
                    <button
                      className="btn secondary"
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => void run(item.id, (id) => cancelSession(id))}
                    >
                      Cancelar
                    </button>
                  </>
                ) : null}
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
