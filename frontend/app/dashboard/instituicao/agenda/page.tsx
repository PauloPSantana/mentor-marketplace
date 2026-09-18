"use client";

import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import { AgendaBoard } from "@/components/dashboard/AgendaBoard";
import { apiErrorMessage } from "@/lib/api";
import { listMentorshipsAsInstitution, type MentorshipRelationship } from "@/lib/mentorships";

export default function InstitutionAgendaPage() {
  const searchParams = useSearchParams();
  const mentorId = searchParams.get("mentor");
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listMentorshipsAsInstitution()
      .then((page) => setMentorships(page.items))
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar as mentorias da agenda.")));
  }, []);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Agenda</h1>
          <p className="post-meta">Calendário das sessões da instituição, com mentor, mentorado, programa e reunião.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <AgendaBoard viewer="INSTITUTION" mentorships={mentorships} mentorProfileId={mentorId} />
    </main>
  );
}
