"use client";

import { useEffect, useState } from "react";
import { SessionStatsCard } from "@/components/mentorships/SessionStatsCard";
import { apiErrorMessage } from "@/lib/api";
import { endOfWeek, startOfWeek } from "@/lib/dashboard";
import { getInstitutionDashboard, type InstitutionDashboard } from "@/lib/institutions";
import { listAgenda, listMentorshipsAsInstitution } from "@/lib/mentorships";

export default function InstitutionReportsPage() {
  const [dashboard, setDashboard] = useState<InstitutionDashboard | null>(null);
  const [weekSessions, setWeekSessions] = useState(0);
  const [activeLinks, setActiveLinks] = useState(0);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const from = startOfWeek();
    const to = endOfWeek();
    Promise.all([
      getInstitutionDashboard(),
      listMentorshipsAsInstitution(),
      listAgenda(from.toISOString(), to.toISOString())
    ])
      .then(([panel, page, sessions]) => {
        setDashboard(panel);
        setActiveLinks(page.items.filter((item) => item.status === "ACTIVE").length);
        setWeekSessions(sessions.filter((item) => item.status === "SCHEDULED").length);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os relatórios.")));
  }, []);

  const pendingInvites = (dashboard?.invitations ?? []).filter((item) => item.status === "PENDING").length;

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Relatórios</h1>
          <p className="post-meta">Indicadores operacionais da instituição.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="stats-grid">
        <article className="stats-card">
          <p className="stats-value">{dashboard?.mentors ?? 0}</p>
          <p className="stats-label">Mentores</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{dashboard?.mentees ?? 0}</p>
          <p className="stats-label">Mentorados</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{activeLinks}</p>
          <p className="stats-label">Mentorias ativas</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{weekSessions}</p>
          <p className="stats-label">Sessões nesta semana</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{pendingInvites}</p>
          <p className="stats-label">Convites pendentes</p>
        </article>
        <article className="stats-card">
          <p className="stats-value">{dashboard?.completedSessions ?? 0}</p>
          <p className="stats-label">Sessões concluídas</p>
        </article>
      </section>
      <SessionStatsCard />
    </main>
  );
}
