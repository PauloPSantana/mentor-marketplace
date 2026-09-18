"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import { apiErrorMessage } from "@/lib/api";
import {
  deactivateInstitutionMentor,
  getInstitutionDashboard,
  type InstitutionMentor
} from "@/lib/institutions";
import {
  formatSessionDate,
  listMentorshipsAsInstitution,
  type MentorshipRelationship
} from "@/lib/mentorships";

export default function InstitutionMentorsPage() {
  const searchParams = useSearchParams();
  const focusId = searchParams.get("mentor");
  const [mentors, setMentors] = useState<InstitutionMentor[]>([]);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([getInstitutionDashboard(), listMentorshipsAsInstitution()])
      .then(([dashboard, page]) => {
        setMentors(dashboard.mentorList);
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os mentores.")));
  }, []);

  const rows = useMemo(() => mentors.map((mentor) => {
    const links = mentorships.filter((item) => item.mentorProfileId === mentor.mentorProfileId);
    const programs = [...new Set(links.map((item) => item.program ?? item.serviceName).filter(Boolean))];
    const next = links
      .map((item) => item.nextSession)
      .filter((session) => session?.status === "SCHEDULED")
      .sort((a, b) => String(a?.scheduledAt).localeCompare(String(b?.scheduledAt)))[0];
    return { mentor, programs, next, menteeCount: links.filter((item) => item.status === "ACTIVE").length };
  }), [mentors, mentorships]);

  async function deactivate(mentorProfileId: string) {
    setBusyId(mentorProfileId);
    try {
      await deactivateInstitutionMentor(mentorProfileId);
      setMentors((current) => current.map((item) => (
        item.mentorProfileId === mentorProfileId ? { ...item, active: false } : item
      )));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível desativar o mentor."));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Mentores</h1>
          <p className="post-meta">Acompanhe programas, mentorados, próxima sessão e status.</p>
        </div>
        <Link href="/dashboard/instituicao/convites" className="btn">Convidar mentor</Link>
      </header>
      {error ? <p className="error">{error}</p> : null}
      {rows.length === 0 ? (
        <p className="feed-empty">Nenhum mentor cadastrado. Envie um convite para começar.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Programa</th>
                <th>Mentorados</th>
                <th>Próxima sessão</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rows.map(({ mentor, programs, next, menteeCount }) => (
                <tr key={mentor.mentorProfileId} style={focusId === mentor.mentorProfileId ? { background: "var(--surface-soft)" } : undefined}>
                  <td>
                    <strong>{mentor.name}</strong>
                    {mentor.specialty ? <p className="post-meta">{mentor.specialty}</p> : null}
                  </td>
                  <td>{programs.join(", ") || mentor.specialty || "—"}</td>
                  <td>{menteeCount || mentor.menteeCount}</td>
                  <td>{next ? formatSessionDate(next.scheduledAt) : "—"}</td>
                  <td>
                    <span className={`status-badge ${mentor.active ? "active" : "paused"}`}>
                      {mentor.active ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td>
                    <div className="row-actions">
                      <Link className="text-btn" href={`/mentors/${mentor.mentorProfileId}`}>Ver perfil</Link>
                      <Link className="text-btn" href={`/dashboard/instituicao/mentorados?mentor=${mentor.mentorProfileId}`}>Ver mentorados</Link>
                      <Link className="text-btn" href={`/dashboard/instituicao/agenda?mentor=${mentor.mentorProfileId}`}>Agenda</Link>
                      {mentor.active ? (
                        <button className="text-btn" type="button" disabled={busyId === mentor.mentorProfileId} onClick={() => void deactivate(mentor.mentorProfileId)}>
                          Desativar
                        </button>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}
