"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { getInstitutionDashboard, type InstitutionInvitation } from "@/lib/institutions";
import { listMentorshipsAsInstitution, type MentorshipRelationship } from "@/lib/mentorships";
import { MENTORSHIP_PROGRAMS } from "@/lib/programs";

export default function InstitutionProgramsPage() {
  const [invitations, setInvitations] = useState<InstitutionInvitation[]>([]);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([getInstitutionDashboard(), listMentorshipsAsInstitution()])
      .then(([dashboard, page]) => {
        setInvitations(dashboard.invitations);
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os programas.")));
  }, []);

  const programs = useMemo(() => {
    const names = new Set<string>([
      ...MENTORSHIP_PROGRAMS,
      ...invitations.map((item) => item.program).filter((item): item is string => Boolean(item)),
      ...mentorships.map((item) => item.program ?? item.serviceName).filter(Boolean)
    ]);
    return [...names].map((name) => {
      const relatedInvites = invitations.filter((item) => item.program === name);
      const relatedMentorships = mentorships.filter((item) => (item.program ?? item.serviceName) === name);
      const mentors = new Set(relatedMentorships.map((item) => item.mentor.id)).size;
      const mentees = new Set(relatedMentorships.map((item) => item.mentee.id)).size;
      return {
        name,
        mentors,
        mentees,
        active: relatedMentorships.filter((item) => item.status === "ACTIVE").length,
        pendingInvites: relatedInvites.filter((item) => item.status === "PENDING").length
      };
    });
  }, [invitations, mentorships]);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Programas</h1>
          <p className="post-meta">Programas da instituição, derivados dos convites e das mentorias ativas.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Programa</th>
              <th>Mentores</th>
              <th>Mentorados</th>
              <th>Mentorias ativas</th>
              <th>Convites pendentes</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {programs.map((program) => (
              <tr key={program.name}>
                <td>{program.name}</td>
                <td>{program.mentors}</td>
                <td>{program.mentees}</td>
                <td>{program.active}</td>
                <td>{program.pendingInvites}</td>
                <td>
                  <Link className="text-btn" href="/dashboard/instituicao/mentorias">Ver mentorias</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </main>
  );
}
