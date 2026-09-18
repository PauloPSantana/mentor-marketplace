"use client";

import Link from "next/link";
import {
  formatSessionDate,
  meetingProviderLabel,
  mentorshipStatusLabel,
  type MentorshipRelationship
} from "@/lib/mentorships";

type MentorshipListProps = {
  items: MentorshipRelationship[];
  perspective: "MENTOR" | "MENTEE" | "INSTITUTION";
};

export function MentorshipList({ items, perspective }: MentorshipListProps) {
  if (items.length === 0) {
    return (
      <p className="feed-empty">
        {perspective === "MENTOR"
          ? "Nenhum mentorado vinculado ainda. Use o formulário acima para associar um mentorado pelo e-mail."
          : perspective === "INSTITUTION"
            ? "Nenhuma mentoria vinculada ainda. Associe um mentorado a um mentor e programa."
            : "Você ainda não possui uma mentoria aceita."}
      </p>
    );
  }

  return (
    <ul className="enrollment-list">
      {items.map((item) => {
        const title = perspective === "INSTITUTION"
          ? `${item.mentee.name} · ${item.mentor.name}`
          : perspective === "MENTOR"
            ? item.mentee.name
            : item.mentor.name;
        return (
          <li key={item.id} className="enrollment-item">
            <div>
              <p className="enrollment-title">{title}</p>
              <p className="post-meta">
                {item.program && item.program !== item.serviceName ? `${item.program} · ` : ""}
                {item.serviceName}
                {item.requiredSessions > 0
                  ? ` · Progresso: ${Math.round((item.completedSessions / item.requiredSessions) * 100)}%`
                  : ` · ${item.completedSessions} sessão(ões) concluída(s)`}
              </p>
              {item.nextSession ? (
                <>
                  <p className="post-meta">
                    Próxima sessão: {formatSessionDate(item.nextSession.scheduledAt)}
                    {item.nextSession.meetingProvider
                      ? ` · ${meetingProviderLabel(item.nextSession.meetingProvider)}`
                      : ""}
                  </p>
                  {item.nextSession.meetingUrl && item.nextSession.status === "SCHEDULED" ? (
                    <p className="post-meta">
                      <a href={item.nextSession.meetingUrl} target="_blank" rel="noreferrer">
                        Entrar na reunião
                      </a>
                    </p>
                  ) : null}
                </>
              ) : null}
              {item.paymentRequired && !item.paymentSettled ? (
                <p className="post-meta">Pagamento pendente</p>
              ) : null}
            </div>
            <div className="enrollment-actions">
              <span className={`status-badge ${item.status.toLowerCase()}`}>
                {mentorshipStatusLabel(item.status)}
              </span>
              <Link className="text-btn" href={`/dashboard/mentorships/${item.id}`}>
                {perspective === "INSTITUTION" ? "Ver mentoria" : "Minha Mentoria"}
              </Link>
            </div>
          </li>
        );
      })}
    </ul>
  );
}
