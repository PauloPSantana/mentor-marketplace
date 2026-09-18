"use client";

import { useEffect, useState } from "react";
import { EnrollmentList, sortEnrollments } from "@/components/enrollments/EnrollmentList";
import { MentorshipList } from "@/components/mentorships/MentorshipList";
import { apiErrorMessage } from "@/lib/api";
import { listSentMentorshipRequests, type Enrollment } from "@/lib/enrollments";
import { listMentorshipsAsMentee, type MentorshipRelationship } from "@/lib/mentorships";

export default function MenteeJourneyPage() {
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([listSentMentorshipRequests(), listMentorshipsAsMentee()])
      .then(([items, page]) => {
        setEnrollments(sortEnrollments(items));
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar sua jornada.")));
  }, []);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Minha Jornada</h1>
          <p className="post-meta">Solicitações e mentorias em andamento.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="enrollment-section">
        <h2>Minhas solicitações</h2>
        <EnrollmentList items={enrollments} perspective="MENTEE" onChange={(items) => setEnrollments(sortEnrollments(items))} />
      </section>
      <section className="enrollment-section">
        <h2>Minhas mentorias</h2>
        <MentorshipList items={mentorships} perspective="MENTEE" />
      </section>
    </main>
  );
}
