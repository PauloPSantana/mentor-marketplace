"use client";

import Link from "next/link";
import {
  formatSessionDate,
  mentorshipStatusLabel,
  type MentorshipRelationship
} from "@/lib/mentorships";

type MentorshipListProps = {
  items: MentorshipRelationship[];
  perspective: "MENTOR" | "MENTEE";
};

export function MentorshipList({ items, perspective }: MentorshipListProps) {
  if (items.length === 0) {
    return (
      <p className="feed-empty">
        {perspective === "MENTOR"
          ? "Nenhuma mentoria ativa ou no histórico."
          : "Você ainda não possui uma mentoria aceita."}
      </p>
    );
  }

  return (
    <ul className="enrollment-list">
      {items.map((item) => {
        const other = perspective === "MENTOR" ? item.mentee : item.mentor;
        return (
          <li key={item.id} className="enrollment-item">
            <div>
              <p className="enrollment-title">{other.name}</p>
              <p className="post-meta">{item.serviceName}</p>
              {item.nextSession ? (
                <p className="post-meta">Próxima sessão: {formatSessionDate(item.nextSession.scheduledAt)}</p>
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
                Ver mentoria
              </Link>
            </div>
          </li>
        );
      })}
    </ul>
  );
}
