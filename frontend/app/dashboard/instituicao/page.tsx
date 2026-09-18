"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { endOfWeek, startOfWeek } from "@/lib/dashboard";
import {
  getInstitutionDashboard,
  invitationStatusClass,
  invitationStatusLabel,
  type InstitutionDashboard,
  type InstitutionInvitation
} from "@/lib/institutions";
import {
  formatSessionDate,
  listAgenda,
  listMentorshipsAsInstitution,
  meetingProviderLabel,
  type MentorshipRelationship,
  type MentorshipSession
} from "@/lib/mentorships";

export default function InstitutionHomePage() {
  const [dashboard, setDashboard] = useState<InstitutionDashboard | null>(null);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [weekSessions, setWeekSessions] = useState<MentorshipSession[]>([]);
  const [error, setError] = useState<string | null>(null);
  const user = getStoredUser();

  useEffect(() => {
    const weekStart = startOfWeek();
    const weekEnd = endOfWeek();
    Promise.all([
      getInstitutionDashboard(),
      listMentorshipsAsInstitution(),
      listAgenda(weekStart.toISOString(), weekEnd.toISOString())
    ])
      .then(([panel, page, sessions]) => {
        setDashboard(panel);
        setMentorships(page.items);
        setWeekSessions(sessions.filter((item) => item.status === "SCHEDULED"));
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o dashboard.")));
  }, []);

  const pendingInvites = useMemo(
    () => (dashboard?.invitations ?? []).filter((item) => item.status === "PENDING"),
    [dashboard]
  );
  const recentInvites = useMemo(
    () => (dashboard?.invitations ?? []).slice(0, 4),
    [dashboard]
  );
  const upcoming = useMemo(
    () => mentorships
      .filter((item) => item.nextSession?.status === "SCHEDULED")
      .sort((a, b) => String(a.nextSession?.scheduledAt).localeCompare(String(b.nextSession?.scheduledAt)))
      .slice(0, 5),
    [mentorships]
  );
  const pendingActivities = useMemo(() => {
    const items: string[] = [];
    if (pendingInvites.length > 0) {
      items.push(`${pendingInvites.length} convite(s) aguardando aceite`);
    }
    const withoutSession = mentorships.filter((item) => item.status === "ACTIVE" && !item.nextSession).length;
    if (withoutSession > 0) {
      items.push(`${withoutSession} mentoria(s) ativa(s) sem próxima sessão`);
    }
    const inactiveMentors = (dashboard?.mentorList ?? []).filter((item) => !item.active).length;
    if (inactiveMentors > 0) {
      items.push(`${inactiveMentors} mentor(es) inativo(s)`);
    }
    return items;
  }, [dashboard, mentorships, pendingInvites.length]);

  const activeMentors = (dashboard?.mentorList ?? []).filter((item) => item.active).length;

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="post-meta">{dashboard?.name ?? "Instituição"}</p>
          <h1>Dashboard</h1>
          <p className="post-meta">Olá{user ? `, ${user.name}` : ""}. Resumo operacional da instituição.</p>
        </div>
      </header>

      {error ? <p className="error">{error}</p> : null}

      <section className="stat-grid">
        <article className="stat-card">
          <p className="stat-value">{activeMentors}</p>
          <p className="stat-label">Mentores ativos</p>
        </article>
        <article className="stat-card">
          <p className="stat-value">{dashboard?.mentees ?? 0}</p>
          <p className="stat-label">Mentorados</p>
        </article>
        <article className="stat-card">
          <p className="stat-value">{dashboard?.activeMentorships ?? 0}</p>
          <p className="stat-label">Mentorias ativas</p>
        </article>
        <article className="stat-card">
          <p className="stat-value">{weekSessions.length}</p>
          <p className="stat-label">Sessões esta semana</p>
        </article>
        <article className="stat-card">
          <p className="stat-value">{pendingInvites.length}</p>
          <p className="stat-label">Convites pendentes</p>
        </article>
      </section>

      <section className="shortcut-grid">
        <Link href="/dashboard/instituicao/mentores" className="shortcut-card">Mentores</Link>
        <Link href="/dashboard/instituicao/mentorados" className="shortcut-card">Mentorados</Link>
        <Link href="/dashboard/instituicao/agenda" className="shortcut-card">Agenda</Link>
        <Link href="/dashboard/instituicao/convites" className="shortcut-card">Convites</Link>
        <Link href="/dashboard/instituicao/mentorias" className="shortcut-card">Mentorias</Link>
        <Link href="/dashboard/instituicao/relatorios" className="shortcut-card">Relatórios</Link>
      </section>

      <div className="dashboard-columns">
        <section className="enrollment-section">
          <div className="section-heading">
            <h2>Próximas sessões</h2>
            <Link href="/dashboard/instituicao/agenda" className="text-btn">Ver agenda</Link>
          </div>
          {upcoming.length === 0 ? (
            <p className="feed-empty">Nenhuma sessão agendada.</p>
          ) : (
            <ul className="enrollment-list">
              {upcoming.map((item) => (
                <li key={item.id} className="enrollment-item">
                  <div>
                    <p className="enrollment-title">{item.mentee.name} · {item.mentor.name}</p>
                    <p className="post-meta">
                      {item.program ?? item.serviceName}
                      {item.nextSession ? ` · ${formatSessionDate(item.nextSession.scheduledAt)}` : ""}
                      {item.nextSession?.meetingProvider ? ` · ${meetingProviderLabel(item.nextSession.meetingProvider)}` : ""}
                    </p>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="enrollment-section">
          <div className="section-heading">
            <h2>Convites recentes</h2>
            <Link href="/dashboard/instituicao/convites" className="text-btn">Ver convites</Link>
          </div>
          {recentInvites.length === 0 ? (
            <p className="feed-empty">Nenhum convite enviado.</p>
          ) : (
            <ul className="enrollment-list">
              {recentInvites.map((invitation: InstitutionInvitation) => (
                <li key={invitation.id} className="enrollment-item">
                  <div>
                    <p className="enrollment-title">{invitation.name}</p>
                    <p className="post-meta">{invitation.email}</p>
                  </div>
                  <span className={`status-badge ${invitationStatusClass(invitation.status)}`}>
                    {invitationStatusLabel(invitation.status)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      <div className="dashboard-columns">
        <section className="enrollment-section">
          <h2>Atividades pendentes</h2>
          {pendingActivities.length === 0 ? (
            <p className="feed-empty">Nada pendente no momento.</p>
          ) : (
            <ul className="plain-list">
              {pendingActivities.map((item) => <li key={item}>{item}</li>)}
            </ul>
          )}
        </section>
        <section className="enrollment-section">
          <h2>Indicadores rápidos</h2>
          <p className="post-meta">Sessões concluídas: {dashboard?.completedSessions ?? 0}</p>
          <p className="post-meta">Mentores cadastrados: {dashboard?.mentors ?? 0}</p>
          <p className="post-meta">Vínculos ativos: {dashboard?.activeMentorships ?? 0}</p>
        </section>
      </div>
    </main>
  );
}
