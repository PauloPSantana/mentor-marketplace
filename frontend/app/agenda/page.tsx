"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { AgendaList } from "@/components/mentorships/AgendaList";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { listAgenda, type MentorshipSession } from "@/lib/mentorships";

export default function AgendaPage() {
  const router = useRouter();
  const [sessions, setSessions] = useState<MentorshipSession[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [isMentor, setIsMentor] = useState(false);

  useEffect(() => {
    const user = getStoredUser();
    if (!user) {
      router.replace("/login");
      return;
    }
    setIsMentor(user.role === "MENTOR" || user.role === "ADMIN");
    listAgenda()
      .then(setSessions)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar a agenda.")))
      .finally(() => setLoading(false));
  }, [router]);

  return (
    <main className="container" style={{ padding: "3rem 0" }}>
      <h1 className="help-heading">
        Agenda
        <HelpTooltip
          text="Aqui ficam as sessões das suas mentorias. Novos horários são criados na página da mentoria."
          href="/ajuda/agenda"
          label="Ajuda sobre a agenda"
        />
      </h1>
      <p className="post-meta">Sessões das suas mentorias, no seu fuso horário.</p>
      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando agenda...</p> : (
        <AgendaList items={sessions} viewerRole={isMentor ? "MENTOR" : "MENTEE"} onChange={setSessions} />
      )}
      <div style={{ marginTop: "1.5rem" }}>
        <Link href="/dashboard/mentorado" className="btn secondary">Voltar ao painel</Link>
      </div>
    </main>
  );
}
