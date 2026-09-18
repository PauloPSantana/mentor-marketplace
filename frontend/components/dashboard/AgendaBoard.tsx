"use client";

import { useEffect, useMemo, useState } from "react";
import { AgendaList } from "@/components/mentorships/AgendaList";
import { apiErrorMessage } from "@/lib/api";
import { addMonths, isSameDay, startOfMonth, startOfWeek } from "@/lib/dashboard";
import {
  formatSessionDate,
  listAgenda,
  meetingProviderLabel,
  sessionStatusLabel,
  type MentorshipRelationship,
  type MentorshipSession
} from "@/lib/mentorships";

type AgendaBoardProps = {
  viewer: "INSTITUTION" | "MENTOR" | "MENTEE";
  mentorships?: MentorshipRelationship[];
  mentorProfileId?: string | null;
};

const WEEKDAYS = ["Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"];

export function AgendaBoard({ viewer, mentorships = [], mentorProfileId }: AgendaBoardProps) {
  const [cursor, setCursor] = useState(() => startOfMonth());
  const [view, setView] = useState<"month" | "week">("month");
  const [selected, setSelected] = useState<Date | null>(null);
  const [sessions, setSessions] = useState<MentorshipSession[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const range = useMemo(() => {
    if (view === "week") {
      const from = startOfWeek(selected ?? new Date());
      const to = new Date(from);
      to.setDate(to.getDate() + 7);
      return { from, to };
    }
    const from = startOfMonth(cursor);
    return { from, to: addMonths(from, 1) };
  }, [cursor, selected, view]);

  useEffect(() => {
    setLoading(true);
    listAgenda(range.from.toISOString(), range.to.toISOString())
      .then(setSessions)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar a agenda.")))
      .finally(() => setLoading(false));
  }, [range.from, range.to]);

  const mentorshipById = useMemo(
    () => new Map(mentorships.map((item) => [item.id, item])),
    [mentorships]
  );

  const visibleSessions = useMemo(() => {
    return sessions.filter((session) => {
      if (!mentorProfileId) return true;
      return mentorshipById.get(session.mentorshipId)?.mentorProfileId === mentorProfileId;
    });
  }, [mentorProfileId, mentorshipById, sessions]);

  const days = useMemo(() => {
    const first = startOfMonth(cursor);
    const pad = (first.getDay() + 6) % 7;
    const start = new Date(first);
    start.setDate(first.getDate() - pad);
    return Array.from({ length: 42 }, (_, index) => {
      const day = new Date(start);
      day.setDate(start.getDate() + index);
      return day;
    });
  }, [cursor]);

  const filtered = useMemo(() => {
    if (!selected) return visibleSessions;
    return visibleSessions.filter((session) => isSameDay(new Date(session.scheduledAt), selected));
  }, [selected, visibleSessions]);

  function mergeSessions(next: MentorshipSession[]) {
    const updatedIds = new Set(next.map((item) => item.id));
    setSessions((current) => {
      const replaced = current.map((item) => next.find((candidate) => candidate.id === item.id) ?? item);
      return replaced.filter((item) => updatedIds.has(item.id) || !filtered.some((visible) => visible.id === item.id));
    });
  }

  return (
    <div>
      <div className="agenda-toolbar">
        <div className="filter-chips">
          <button className={`filter-chip${view === "month" ? " active" : ""}`} type="button" onClick={() => setView("month")}>
            Mensal
          </button>
          <button className={`filter-chip${view === "week" ? " active" : ""}`} type="button" onClick={() => setView("week")}>
            Semanal
          </button>
        </div>
        {view === "month" ? (
          <div className="agenda-month-nav">
            <button className="text-btn" type="button" onClick={() => setCursor((current) => addMonths(current, -1))}>←</button>
            <strong>{cursor.toLocaleDateString("pt-BR", { month: "long", year: "numeric" })}</strong>
            <button className="text-btn" type="button" onClick={() => setCursor((current) => addMonths(current, 1))}>→</button>
          </div>
        ) : (
          <p className="post-meta">Semana de {startOfWeek(selected ?? new Date()).toLocaleDateString("pt-BR")}</p>
        )}
      </div>

      {view === "month" ? (
        <div className="agenda-calendar">
          {WEEKDAYS.map((label) => <div key={label} className="agenda-weekday">{label}</div>)}
          {days.map((day) => {
            const inMonth = day.getMonth() === cursor.getMonth();
            const count = visibleSessions.filter((session) => isSameDay(new Date(session.scheduledAt), day)).length;
            const active = selected ? isSameDay(day, selected) : false;
            return (
              <button
                key={day.toISOString()}
                type="button"
                className={`agenda-day${inMonth ? "" : " muted"}${active ? " active" : ""}`}
                onClick={() => setSelected(day)}
              >
                <span>{day.getDate()}</span>
                {count > 0 ? <small>{count}</small> : null}
              </button>
            );
          })}
        </div>
      ) : null}

      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando agenda...</p> : null}

      {viewer === "INSTITUTION" ? (
        <InstitutionSessionTable sessions={filtered} mentorshipById={mentorshipById} />
      ) : (
        <AgendaList
          items={filtered}
          viewerRole={viewer}
          onChange={mergeSessions}
        />
      )}
    </div>
  );
}

function InstitutionSessionTable({
  sessions,
  mentorshipById
}: {
  sessions: MentorshipSession[];
  mentorshipById: Map<string, MentorshipRelationship>;
}) {
  if (sessions.length === 0) {
    return <p className="feed-empty">Nenhuma sessão neste período.</p>;
  }

  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Quando</th>
            <th>Mentor</th>
            <th>Mentorado</th>
            <th>Programa</th>
            <th>Reunião</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {sessions.map((session) => {
            const mentorship = mentorshipById.get(session.mentorshipId);
            return (
              <tr key={session.id}>
                <td>{formatSessionDate(session.scheduledAt)}</td>
                <td>{mentorship?.mentor.name ?? session.participant.name}</td>
                <td>{mentorship?.mentee.name ?? "—"}</td>
                <td>{mentorship?.program ?? mentorship?.serviceName ?? "—"}</td>
                <td>
                  {session.meetingUrl ? (
                    <a href={session.meetingUrl} target="_blank" rel="noreferrer">
                      {meetingProviderLabel(session.meetingProvider)}
                    </a>
                  ) : meetingProviderLabel(session.meetingProvider)}
                </td>
                <td>
                  <span className={`status-badge ${session.status.toLowerCase()}`}>
                    {sessionStatusLabel(session.status)}
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
