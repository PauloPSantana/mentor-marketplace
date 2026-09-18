"use client";

import Link from "next/link";

export default function MentorMessagesPage() {
  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Mensagens</h1>
          <p className="post-meta">O chat ainda não está disponível. Por enquanto, use as notificações para acompanhar comentários, sessões e convites.</p>
        </div>
      </header>
      <Link href="/notifications" className="btn">Abrir notificações</Link>
    </main>
  );
}
