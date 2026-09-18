"use client";

import { getSessionStats, type SessionStats } from "@/lib/mentorships";
import { useEffect, useState } from "react";

function percent(value: number): string {
  return `${Math.round(value * 100)}%`;
}

function hours(minutes: number): string {
  const value = minutes / 60;
  return `${value.toLocaleString("pt-BR", { maximumFractionDigits: 1 })} h`;
}

export function SessionStatsCard() {
  const [stats, setStats] = useState<SessionStats | null>(null);

  useEffect(() => {
    getSessionStats().then(setStats).catch(() => undefined);
  }, []);

  if (!stats) {
    return null;
  }

  return (
    <section className="enrollment-section">
      <h2>Sessões</h2>
      <p className="post-meta">
        {stats.completedCount} concluídas · {hours(stats.completedMinutes)} de mentoria · presença {percent(stats.attendanceRate)} · conclusão {percent(stats.completionRate)}
      </p>
      <p className="post-meta">
        {stats.scheduledCount} agendada(s) · {stats.noShowCount} ausência(s) · {stats.cancelledCount} cancelada(s)
      </p>
    </section>
  );
}
