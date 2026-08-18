"use client";

import Link from "next/link";
import { useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  acceptEnrollment,
  canCompleteEnrollment,
  cancelEnrollment,
  completeEnrollment,
  enrollmentDisplayStatus,
  enrollmentStatusLabel,
  formatMoney,
  rejectEnrollment,
  type Enrollment,
  type EnrollmentStatus
} from "@/lib/enrollments";

type EnrollmentListProps = {
  items: Enrollment[];
  perspective: "MENTEE" | "MENTOR";
  onChange?: (items: Enrollment[]) => void;
};

export function EnrollmentList({ items, perspective, onChange }: EnrollmentListProps) {
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  if (items.length === 0) {
    return (
      <p className="feed-empty">
        {perspective === "MENTOR"
          ? "Nenhuma solicitação de mentoria por enquanto."
          : "Você ainda não solicitou uma mentoria."}
      </p>
    );
  }

  async function runAction(id: string, action: (id: string) => Promise<Enrollment>) {
    setError(null);
    setBusyId(id);
    try {
      const updated = await action(id);
      onChange?.(items.map((item) => (item.id === id ? updated : item)));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar a solicitação."));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <div>
      {error ? <p className="error">{error}</p> : null}
      <ul className="enrollment-list">
        {items.map((item) => {
          const displayStatus = enrollmentDisplayStatus(item);
          return (
            <li key={item.id} className="enrollment-item">
              <div>
                <p className="enrollment-title">
                  {perspective === "MENTOR" ? item.menteeName : item.mentorName}
                </p>
                <p className="post-meta">{item.productTitle}</p>
                {item.message ? <p className="post-content">{item.message}</p> : null}
                <p className="post-meta">{formatMoney(Number(item.priceSnapshot), item.currency)}</p>
              </div>
              <div className="enrollment-actions">
                <span className={`status-badge ${displayStatus.toLowerCase()}`}>
                  {enrollmentStatusLabel(item)}
                </span>
                {perspective === "MENTEE" ? (
                  <Link className="text-btn" href={`/mentors/${item.mentorProfileId}`}>
                    Ver mentor
                  </Link>
                ) : null}
                {perspective === "MENTEE" && item.status === "PENDING" ? (
                  <button
                    className="btn secondary"
                    type="button"
                    disabled={busyId === item.id}
                    onClick={() => void runAction(item.id, cancelEnrollment)}
                  >
                    Cancelar
                  </button>
                ) : null}
                {perspective === "MENTOR" && item.status === "PENDING" ? (
                  <>
                    <button
                      className="btn"
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => void runAction(item.id, acceptEnrollment)}
                    >
                      Aceitar
                    </button>
                    <button
                      className="btn secondary"
                      type="button"
                      disabled={busyId === item.id}
                      onClick={() => void runAction(item.id, rejectEnrollment)}
                    >
                      Recusar
                    </button>
                  </>
                ) : null}
                {perspective === "MENTOR" && canCompleteEnrollment(item) ? (
                  <button
                    className="btn secondary"
                    type="button"
                    disabled={busyId === item.id}
                    onClick={() => void runAction(item.id, completeEnrollment)}
                  >
                    Concluir
                  </button>
                ) : null}
              </div>
            </li>
          );
        })}
      </ul>
    </div>
  );
}

export function sortEnrollments(items: Enrollment[]): Enrollment[] {
  const rank: Record<EnrollmentStatus, number> = {
    PENDING: 0,
    ACCEPTED: 1,
    ACTIVE: 1,
    COMPLETED: 2,
    REJECTED: 3,
    CANCELLED: 4,
    EXPIRED: 5
  };
  return [...items].sort(
    (a, b) => rank[enrollmentDisplayStatus(a)] - rank[enrollmentDisplayStatus(b)] || b.createdAt.localeCompare(a.createdAt)
  );
}
