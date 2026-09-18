"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { SessionStatsCard } from "@/components/mentorships/SessionStatsCard";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { endOfWeek, startOfWeek } from "@/lib/dashboard";
import {
  formatSessionDate,
  listAgenda,
  listMentorshipsAsMentor,
  meetingProviderLabel,
  type MentorshipRelationship,
  type MentorshipSession
} from "@/lib/mentorships";

export default function MentorHomePage() {
  const user = getStoredUser();
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [weekSessions, setWeekSessions] = useState<MentorshipSession[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const from = startOfWeek();
    const to = endOfWeek();
    Promise.all([listMentorshipsAsMentor(), listAgenda(from.toISOString(), to.toISOString())])
      .then(([page, sessions]) => {
        setMentorships(page.items);
        setWeekSessions(sessions.filter((item) => item.status === "SCHEDULED"));
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o dashboard.")));
  }, []);

  const upcoming = useMemo(
    () => mentorships
      .filter((item) => item.nextSession?.status === "SCHEDULED")
      .sort((a, b) => String(a.nextSession?.scheduledAt).localeCompare(String(b.nextSession?.scheduledAt)))
      .slice(0, 5),
    [mentorships]
  );
  const activeMentees = mentorships.filter((item) => item.status === "ACTIVE" || item.status === "PAUSED");

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Dashboard</h1>
          <p className="post-meta">Olá{user ? `, ${user.name}` : ""}. Resumo do seu trabalho com os mentorados.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="stats-grid">
        <article className="stats-card">
          <p className="stats-value">{activeMentees.length}</p>
          <p className="stats-label">Meus mentorados</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{weekSessions.length}</p>
          <p className="stats-label">Sessões esta semana</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{upcoming.length}</p>
          <p className="stats-label">Próximas sessões</p>
        </article>
      </section>
      <section className="shortcut-grid">
        <Link href="/dashboard/mentor/mentorados" className="shortcut-card">Meus Mentorados</Link>
        <Link href="/dashboard/mentor/agenda" className="shortcut-card">Agenda</Link>
        <Link href="/dashboard/mentor/plano" className="shortcut-card">Plano de Estudos</Link>
        <Link href="/dashboard/mentor/tarefas" className="shortcut-card">Tarefas</Link>
        <Link href="/dashboard/mentor/questionarios" className="shortcut-card">Questionários</Link>
        <Link href="/dashboard/mentor/mensagens" className="shortcut-card">Mensagens</Link>
      </section>
      <section className="enrollment-section">
        <div className="section-heading">
          <h2>Próximas sessões</h2>
          <Link href="/dashboard/mentor/agenda" className="text-btn">Ver agenda</Link>
        </div>
        {upcoming.length === 0 ? (
          <p className="feed-empty">Nenhuma sessão agendada.</p>
        ) : (
          <ul className="enrollment-list">
            {upcoming.map((item) => (
              <li key={item.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">{item.mentee.name}</p>
                  <p className="post-meta">
                    {item.program ?? item.serviceName}
                    {item.nextSession ? ` · ${formatSessionDate(item.nextSession.scheduledAt)}` : ""}
                    {item.nextSession?.meetingProvider ? ` · ${meetingProviderLabel(item.nextSession.meetingProvider)}` : ""}
                  </p>
                </div>
                <Link className="text-btn" href={`/dashboard/mentorships/${item.id}?tab=sessoes`}>Abrir</Link>
              </li>
            ))}
          </ul>
        )}
      </section>
      <SessionStatsCard />
    </main>
  );
}
