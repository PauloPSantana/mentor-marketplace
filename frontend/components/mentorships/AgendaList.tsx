"use client";

import { useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  cancelSession,
  completeSession,
  formatSessionDate,
  markSessionNoShow,
  sessionStatusLabel,
  type MentorshipSession
} from "@/lib/mentorships";

type AgendaListProps = {
  items: MentorshipSession[];
  viewerRole?: "MENTOR" | "MENTEE";
  onChange?: (items: MentorshipSession[]) => void;
};

export function AgendaList({ items, viewerRole, onChange }: AgendaListProps) {
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  if (items.length === 0) {
    return <p className="feed-empty">Nenhuma sessão neste período.</p>;
  }

  async function run(id: string, action: (id: string) => Promise<MentorshipSession>) {
    setError(null);
    setBusyId(id);
    try {
      const updated = await action(id);
      onChange?.(items.map((item) => (item.id === id ? updated : item)));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar a sessão."));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      {error ? <p className="error">{error}</p> : null}
      <ul className="enrollment-list">
        {items.map((item) => (
          <li key={item.id} className="enrollment-item">
            <div>
              <p className="enrollment-title">{item.participant.name}</p>
              <p className="post-meta">{formatSessionDate(item.scheduledAt)} · {item.durationMinutes} min</p>
              {item.meetingUrl ? (
                <p className="post-meta">
                  <a href={item.meetingUrl} target="_blank" rel="noreferrer">
                    Entrar na reunião
                  </a>
                </p>
              ) : null}
              {item.notes ? <p className="post-content">{item.notes}</p> : null}
            </div>
            <div className="enrollment-actions">
              <span className={`status-badge ${item.status.toLowerCase()}`}>
                {sessionStatusLabel(item.status)}
              </span>
              {item.status === "SCHEDULED" && viewerRole === "MENTEE" ? (
                <button
                  className="btn secondary"
                  type="button"
                  disabled={busyId === item.id}
                  onClick={() => void run(item.id, (id) => cancelSession(id))}
                >
                  Cancelar
                </button>
              ) : null}
              {item.status === "SCHEDULED" && viewerRole === "MENTOR" ? (
                <>
                  <button className="btn" type="button" disabled={busyId === item.id} onClick={() => void run(item.id, completeSession)}>
                    Concluir
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
        ))}
      </ul>
    </div>
  );
}
