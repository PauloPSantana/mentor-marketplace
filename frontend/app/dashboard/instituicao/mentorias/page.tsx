"use client";

import { useEffect, useState } from "react";
import { AssignMentorshipForm } from "@/components/mentorships/AssignMentorshipForm";
import { MentorshipList } from "@/components/mentorships/MentorshipList";
import { apiErrorMessage } from "@/lib/api";
import { getInstitutionDashboard } from "@/lib/institutions";
import { listMentorshipsAsInstitution, type MentorshipRelationship } from "@/lib/mentorships";

export default function InstitutionMentorshipsPage() {
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [mentors, setMentors] = useState<{ mentorProfileId: string; name: string; active: boolean }[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([getInstitutionDashboard(), listMentorshipsAsInstitution()])
      .then(([dashboard, page]) => {
        setMentors(dashboard.mentorList.map((item) => ({
          mentorProfileId: item.mentorProfileId,
          name: item.name,
          active: item.active
        })));
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar as mentorias.")));
  }, []);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Mentorias</h1>
          <p className="post-meta">Relações mentor ↔ mentorado e vínculo a um programa.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="enrollment-section">
        <h2>Vincular mentorado</h2>
        <AssignMentorshipForm
          mentors={mentors}
          onAssigned={(created) => setMentorships((current) => [created, ...current.filter((item) => item.id !== created.id)])}
        />
      </section>
      <section className="enrollment-section">
        <h2>Vínculos</h2>
        <MentorshipList items={mentorships} perspective="INSTITUTION" />
      </section>
    </main>
  );
}
