"use client";

import { AgendaBoard } from "@/components/dashboard/AgendaBoard";

export default function MenteeAgendaPage() {
  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Agenda</h1>
          <p className="post-meta">Suas sessões agendadas, no seu fuso horário.</p>
        </div>
      </header>
      <AgendaBoard viewer="MENTEE" />
    </main>
  );
}
