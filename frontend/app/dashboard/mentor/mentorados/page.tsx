"use client";

import { useEffect, useState } from "react";
import { EnrollmentList, sortEnrollments } from "@/components/enrollments/EnrollmentList";
import { AssignMentorshipForm } from "@/components/mentorships/AssignMentorshipForm";
import { MentorshipList } from "@/components/mentorships/MentorshipList";
import { apiErrorMessage } from "@/lib/api";
import { listReceivedMentorshipRequests, type Enrollment } from "@/lib/enrollments";
import { listMentorshipsAsMentor, type MentorshipRelationship } from "@/lib/mentorships";

export default function MentorMenteesPage() {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([listReceivedMentorshipRequests(), listMentorshipsAsMentor()])
      .then(([items, page]) => {
        setEnrollments(sortEnrollments(items));
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os mentorados.")));
  }, []);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Meus Mentorados</h1>
          <p className="post-meta">Solicitações, vínculos e acompanhamento dos seus mentorados.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="enrollment-section">
        <h2>Solicitações</h2>
        <EnrollmentList items={enrollments} perspective="MENTOR" onChange={(items) => setEnrollments(sortEnrollments(items))} />
      </section>
      <section className="enrollment-section">
        <h2>Vincular mentorado</h2>
        <AssignMentorshipForm
          onAssigned={(created) => setMentorships((current) => [created, ...current.filter((item) => item.id !== created.id)])}
        />
      </section>
      <section className="enrollment-section">
        <h2>Mentorados</h2>
        <MentorshipList items={mentorships} perspective="MENTOR" />
      </section>
    </main>
  );
}
