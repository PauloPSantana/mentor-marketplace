"use client";

import { AgendaBoard } from "@/components/dashboard/AgendaBoard";

export default function MentorAgendaPage() {
  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Agenda</h1>
          <p className="post-meta">Sessões das suas mentorias, com opção de reagendar, concluir ou cancelar.</p>
        </div>
      </header>
      <AgendaBoard viewer="MENTOR" />
    </main>
  );
}
