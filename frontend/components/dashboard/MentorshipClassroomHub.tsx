"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { classroomPath } from "@/lib/classroom";
import {
  formatSessionDate,
  listMentorshipsAsMentee,
  listMentorshipsAsMentor,
  mentorshipStatusLabel,
  type MentorshipRelationship
} from "@/lib/mentorships";

type MentorshipClassroomHubProps = {
  title: string;
  description: string;
  empty: string;
  tab: "plano" | "tarefas" | "questionarios" | "sessoes" | "evolucao";
  actionLabel: string;
  perspective: "MENTOR" | "MENTEE";
};

export function MentorshipClassroomHub({
  title,
  description,
  empty,
  tab,
  actionLabel,
  perspective
}: MentorshipClassroomHubProps) {
  const [items, setItems] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const request = perspective === "MENTOR" ? listMentorshipsAsMentor() : listMentorshipsAsMentee();
    request
      .then((page) => setItems(page.items))
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar as mentorias.")));
  }, [perspective]);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>{title}</h1>
          <p className="post-meta">{description}</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      {items.length === 0 && !error ? (
        <p className="feed-empty">{empty}</p>
      ) : (
        <ul className="enrollment-list">
          {items.map((item) => {
            const other = perspective === "MENTOR" ? item.mentee : item.mentor;
            return (
              <li key={item.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">{other.name}</p>
                  <p className="post-meta">
                    {item.program ?? item.serviceName}
                    {" · "}
                    {mentorshipStatusLabel(item.status)}
                    {item.nextSession
                      ? ` · Próxima sessão: ${formatSessionDate(item.nextSession.scheduledAt)}`
                      : ""}
                  </p>
                </div>
                <Link className="btn secondary" href={classroomPath(item.id, tab)}>
                  {actionLabel}
                </Link>
              </li>
            );
          })}
        </ul>
      )}
    </main>
  );
}
