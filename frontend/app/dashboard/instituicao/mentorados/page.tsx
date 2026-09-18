"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "next/navigation";
import { apiErrorMessage } from "@/lib/api";
import {
  formatSessionDate,
  listMentorshipsAsInstitution,
  mentorshipStatusLabel,
  type MentorshipRelationship
} from "@/lib/mentorships";

export default function InstitutionMenteesPage() {
  const searchParams = useSearchParams();
  const mentorId = searchParams.get("mentor");
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listMentorshipsAsInstitution()
      .then((page) => setMentorships(page.items))
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os mentorados.")));
  }, []);

  const rows = useMemo(() => {
    const filtered = mentorId
      ? mentorships.filter((item) => item.mentorProfileId === mentorId)
      : mentorships;
    const unique = new Map<string, MentorshipRelationship>();
    for (const item of filtered) {
      const current = unique.get(item.mentee.id);
      if (!current || (item.nextSession && !current.nextSession)) {
        unique.set(item.mentee.id, item);
      }
    }
    return [...unique.values()];
  }, [mentorId, mentorships]);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Mentorados</h1>
          <p className="post-meta">Gestão dos mentorados vinculados aos mentores da instituição.</p>
        </div>
        <Link href="/dashboard/instituicao/mentorias" className="btn">Vincular mentorado</Link>
      </header>
      {error ? <p className="error">{error}</p> : null}
      {rows.length === 0 ? (
        <p className="feed-empty">Nenhum mentorado vinculado ainda.</p>
      ) : (
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Mentor</th>
                <th>Programa</th>
                <th>Próxima sessão</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {rows.map((item) => (
                <tr key={item.mentee.id}>
                  <td>{item.mentee.name}</td>
                  <td>{item.mentor.name}</td>
                  <td>{item.program ?? item.serviceName}</td>
                  <td>{item.nextSession ? formatSessionDate(item.nextSession.scheduledAt) : "—"}</td>
                  <td>
                    <span className={`status-badge ${item.status.toLowerCase()}`}>
                      {mentorshipStatusLabel(item.status)}
                    </span>
                  </td>
                  <td>
                    <Link className="text-btn" href={`/dashboard/mentorships/${item.id}`}>
                      Ver mentoria
                    </Link>
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
