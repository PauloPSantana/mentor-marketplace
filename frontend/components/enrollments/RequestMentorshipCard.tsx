"use client";

import { FormEvent, useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import {
  cancelEnrollment,
  enrollmentStatusLabel,
  formatMoney,
  getMyEnrollmentForMentor,
  isOpenEnrollment,
  requestMentorship,
  type Enrollment
} from "@/lib/enrollments";

type RequestMentorshipCardProps = {
  mentorId: string;
  sessionPrice: number | null;
  blocked?: boolean;
};

export function RequestMentorshipCard({ mentorId, sessionPrice, blocked = false }: RequestMentorshipCardProps) {
  const currentUser = getStoredUser();
  const [enrollment, setEnrollment] = useState<Enrollment | null>(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    if (currentUser?.role !== "MENTEE") {
      setLoading(false);
      return;
    }
    let cancelled = false;
    getMyEnrollmentForMentor(mentorId)
      .then((current) => {
        if (!cancelled) {
          setEnrollment(current);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, "Não foi possível verificar sua solicitação."));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [mentorId, currentUser?.role]);

  if (currentUser?.role !== "MENTEE" || blocked) {
    return null;
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const created = await requestMentorship(mentorId, message);
      setEnrollment(created);
      setMessage("");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível enviar a solicitação."));
    } finally {
      setSubmitting(false);
    }
  }

  async function onCancel() {
    if (!enrollment) {
      return;
    }
    setError(null);
    setCancelling(true);
    try {
      const updated = await cancelEnrollment(enrollment.id);
      setEnrollment(updated);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível cancelar a solicitação."));
    } finally {
      setCancelling(false);
    }
  }

  const openEnrollment = enrollment && isOpenEnrollment(enrollment);

  return (
    <section className="enrollment-card">
      <h2 className="help-heading">
        Solicitar mentoria
        <HelpTooltip
          text="Explique brevemente seus objetivos para que o mentor possa avaliar sua solicitação."
          href="/ajuda/mentorias"
          label="Ajuda sobre solicitar mentoria"
        />
      </h2>
      {sessionPrice != null && Number(sessionPrice) > 0 ? (
        <p className="post-meta">Valor da sessão: {formatMoney(Number(sessionPrice))}</p>
      ) : (
        <p className="post-meta">Valor da sessão a combinar com o mentor.</p>
      )}
      {loading ? <p className="feed-status">Carregando...</p> : null}
      {openEnrollment ? (
        <div>
          <p>
            Status da sua solicitação: <strong>{enrollmentStatusLabel(enrollment)}</strong>
          </p>
          {enrollment.status === "PENDING" ? (
            <button className="btn secondary" type="button" disabled={cancelling} onClick={() => void onCancel()}>
              {cancelling ? "Cancelando..." : "Cancelar solicitação"}
            </button>
          ) : null}
          {error ? <p className="error">{error}</p> : null}
        </div>
      ) : (
        <form onSubmit={onSubmit} className="card-form" style={{ display: "grid", gap: "0.75rem" }}>
          <label>
            Mensagem (opcional)
            <textarea
              className="input"
              rows={3}
              maxLength={2000}
              value={message}
              onChange={(event) => setMessage(event.target.value)}
              placeholder="Conte rapidamente o que você busca nesta mentoria"
            />
          </label>
          <p className="help-hint">
            <span aria-hidden="true">ⓘ</span>
            Explique brevemente seus objetivos para que o mentor possa avaliar sua solicitação.
          </p>
          {error ? <p className="error">{error}</p> : null}
          <button className="btn" type="submit" disabled={submitting || loading}>
            {submitting ? "Enviando..." : "Solicitar mentoria"}
          </button>
        </form>
      )}
    </section>
  );
}
