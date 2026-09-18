"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { SessionStatsCard } from "@/components/mentorships/SessionStatsCard";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  formatSessionDate,
  listMentorshipsAsMentee,
  meetingProviderLabel,
  type MentorshipRelationship
} from "@/lib/mentorships";

export default function MenteeHomePage() {
  const user = getStoredUser();
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listMentorshipsAsMentee()
      .then((page) => setMentorships(page.items))
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o dashboard.")));
  }, []);

  const current = mentorships.find((item) => item.status === "ACTIVE") ?? mentorships[0] ?? null;
  const nextSession = current?.nextSession?.status === "SCHEDULED" ? current.nextSession : null;
  const progress = useMemo(() => {
    if (!current) return 0;
    if (current.requiredSessions <= 0) return current.completedSessions > 0 ? 100 : 0;
    return Math.round((current.completedSessions / current.requiredSessions) * 100);
  }, [current]);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Dashboard</h1>
          <p className="post-meta">Olá{user ? `, ${user.name}` : ""}. Sua jornada de mentoria em um só lugar.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="stats-grid">
        <article className="stats-card">
          <p className="stats-label">Meu mentor</p>
          <p className="stats-value" style={{ fontSize: "1.15rem" }}>{current?.mentor.name ?? "—"}</p>
        </article>
        <article className="stats-card">
          <p className="stats-label">Próxima sessão</p>
          <p className="stats-value" style={{ fontSize: "1.05rem" }}>
            {nextSession ? formatSessionDate(nextSession.scheduledAt) : "Não agendada"}
          </p>
          {nextSession?.meetingProvider ? (
            <p className="post-meta">{meetingProviderLabel(nextSession.meetingProvider)}</p>
          ) : null}
        </article>
        <article className="stats-card">
          <p className="stats-value">{progress}%</p>
          <p className="stats-label">Progresso</p>
        </article>
      </section>
      <section className="shortcut-grid">
        <Link href="/dashboard/mentorado/jornada" className="shortcut-card">Minha Jornada</Link>
        <Link href="/dashboard/mentorado/agenda" className="shortcut-card">Agenda</Link>
        <Link href="/dashboard/mentorado/plano" className="shortcut-card">Plano de Estudos</Link>
        <Link href="/dashboard/mentorado/tarefas" className="shortcut-card">Tarefas</Link>
        <Link href="/dashboard/mentorado/questionarios" className="shortcut-card">Questionários</Link>
        <Link href="/dashboard/mentorado/progresso" className="shortcut-card">Progresso</Link>
      </section>
      {nextSession?.meetingUrl ? (
        <p className="post-meta">
          <a href={nextSession.meetingUrl} target="_blank" rel="noreferrer">Entrar na reunião</a>
        </p>
      ) : null}
      <SessionStatsCard />
    </main>
  );
}
