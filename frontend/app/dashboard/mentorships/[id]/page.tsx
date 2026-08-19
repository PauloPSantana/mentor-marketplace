"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AgendaList } from "@/components/mentorships/AgendaList";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  cancelMentorship,
  completeMentorship,
  createMentorshipSession,
  formatSessionDate,
  getMentorshipRelationship,
  listMentorshipSessions,
  mentorshipStatusLabel,
  type MentorshipRelationship,
  type MentorshipSession
} from "@/lib/mentorships";
import {
  confirmPayment,
  createMentorshipPayment,
  formatMoney,
  listMentorshipPayments,
  paymentStatusLabel,
  type Payment
} from "@/lib/payments";
import { createMentorshipReview, listMentorshipReviews, type Review } from "@/lib/reviews";

export default function MentorshipDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const currentUser = getStoredUser();
  const userId = currentUser?.id;
  const [mentorship, setMentorship] = useState<MentorshipRelationship | null>(null);
  const [sessions, setSessions] = useState<MentorshipSession[]>([]);
  const [payments, setPayments] = useState<Payment[]>([]);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [paying, setPaying] = useState(false);
  const [scheduledAt, setScheduledAt] = useState("");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [meetingUrl, setMeetingUrl] = useState("");
  const [notes, setNotes] = useState("");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");

  const isMentor = currentUser?.id === mentorship?.mentor.id || currentUser?.role === "ADMIN";
  const isMentee = currentUser?.id === mentorship?.mentee.id;
  const isParticipant = Boolean(mentorship && (currentUser?.id === mentorship.mentor.id || currentUser?.id === mentorship.mentee.id));
  const myReview = reviews.find((review) => review.reviewerId === currentUser?.id);
  const completedSessions = sessions.filter((session) => session.status === "COMPLETED");
  const canSchedule = mentorship?.status === "ACTIVE" && isParticipant && (!mentorship.paymentRequired || mentorship.paymentSettled);

  useEffect(() => {
    if (!userId) {
      router.replace("/login");
      return;
    }
    Promise.all([
      getMentorshipRelationship(params.id),
      listMentorshipSessions(params.id),
      listMentorshipPayments(params.id),
      listMentorshipReviews(params.id).catch(() => [] as Review[])
    ])
      .then(([relationship, items, charges, mentorshipReviews]) => {
        setMentorship(relationship);
        setSessions(items);
        setPayments(charges);
        setReviews(mentorshipReviews);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar a mentoria.")))
      .finally(() => setLoading(false));
  }, [params.id, router, userId]);

  const other = useMemo(() => {
    if (!mentorship || !currentUser) return null;
    return currentUser.id === mentorship.mentor.id ? mentorship.mentee : mentorship.mentor;
  }, [mentorship, currentUser]);

  async function refreshRelationship() {
    const [relationship, charges] = await Promise.all([
      getMentorshipRelationship(params.id),
      listMentorshipPayments(params.id)
    ]);
    setMentorship(relationship);
    setPayments(charges);
  }

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!mentorship) return;
    setError(null);
    setSaving(true);
    try {
      const created = await createMentorshipSession(mentorship.id, {
        scheduledAt: new Date(scheduledAt).toISOString(),
        durationMinutes,
        meetingUrl,
        notes
      });
      setSessions((current) => [...current, created].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)));
      setScheduledAt("");
      setMeetingUrl("");
      setNotes("");
      await refreshRelationship();
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível agendar a sessão."));
    } finally {
      setSaving(false);
    }
  }

  async function onPay() {
    if (!mentorship) return;
    setError(null);
    setPaying(true);
    try {
      const charge = await createMentorshipPayment(mentorship.id);
      const paid = charge.status === "PAID" ? charge : await confirmPayment(charge.id);
      setPayments((current) => {
        const others = current.filter((item) => item.id !== paid.id);
        return [paid, ...others];
      });
      await refreshRelationship();
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível confirmar o pagamento."));
    } finally {
      setPaying(false);
    }
  }

  async function onCompleteMentorship() {
    if (!mentorship) return;
    setError(null);
    try {
      setMentorship(await completeMentorship(mentorship.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível concluir a mentoria."));
    }
  }

  async function onCancelMentorship() {
    if (!mentorship) return;
    setError(null);
    try {
      setMentorship(await cancelMentorship(mentorship.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível cancelar a mentoria."));
    }
  }

  async function onReview(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!mentorship) return;
    setError(null);
    try {
      const saved = await createMentorshipReview(mentorship.id, { rating, comment });
      setReviews((current) => {
        const others = current.filter((item) => item.reviewerId !== saved.reviewerId);
        return [saved, ...others];
      });
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível enviar a avaliação."));
    }
  }

  if (loading) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  if (!mentorship) {
    return (
      <main className="container" style={{ padding: "3rem 0" }}>
        <p className="error">{error ?? "Mentoria não encontrada."}</p>
      </main>
    );
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 760 }}>
      <p className="post-meta">
        <Link href="/agenda">Agenda</Link>
      </p>
      <h1>{mentorship.serviceName}</h1>
      <p>
        Com {other?.name}.{" "}
        <span className={`status-badge ${mentorship.status.toLowerCase()}`}>
          {mentorshipStatusLabel(mentorship.status)}
        </span>
      </p>
      <p className="post-meta">
        Sessões concluídas: {mentorship.completedSessions}/{mentorship.requiredSessions}
        {mentorship.scheduledSessions > 0 ? ` • ${mentorship.scheduledSessions} agendada(s)` : ""}
      </p>
      {mentorship.nextSession ? (
        <p className="post-meta">Próxima sessão: {formatSessionDate(mentorship.nextSession.scheduledAt)}</p>
      ) : null}
      {error ? <p className="error">{error}</p> : null}

      {mentorship.paymentRequired ? (
        <section className="enrollment-section">
          <h2 className="help-heading">
            Pagamento
            <HelpTooltip
              text="Se houver valor de sessão, o mentorado confirma o pagamento antes de agendar."
              href="/ajuda/pagamentos"
              label="Ajuda sobre pagamento"
            />
          </h2>
          <p>
            Valor: {formatMoney(mentorship.amountDue, mentorship.currency)}.{" "}
            {mentorship.paymentSettled ? "Pagamento confirmado." : "Aguardando pagamento do mentorado."}
          </p>
          {payments.length > 0 ? (
            <ul className="enrollment-list">
              {payments.map((payment) => (
                <li key={payment.id} className="enrollment-item">
                  <div>
                    <p className="enrollment-title">{formatMoney(payment.amount, payment.currency)}</p>
                    <p className="post-meta">{payment.providerTransactionId}</p>
                  </div>
                  <span className={`status-badge ${payment.status.toLowerCase()}`}>
                    {paymentStatusLabel(payment.status)}
                  </span>
                </li>
              ))}
            </ul>
          ) : null}
          {isMentee && !mentorship.paymentSettled && mentorship.status === "ACTIVE" ? (
            <button className="btn" type="button" disabled={paying} onClick={() => void onPay()}>
              {paying ? "Confirmando..." : `Pagar ${formatMoney(mentorship.amountDue, mentorship.currency)}`}
            </button>
          ) : null}
        </section>
      ) : null}

      {canSchedule ? (
        <form className="card-form" onSubmit={onCreate} style={{ display: "grid", gap: "0.75rem", margin: "1.5rem 0" }}>
          <h2>Agendar sessão</h2>
          <label>
            Data e horário
            <input className="input" type="datetime-local" required value={scheduledAt} onChange={(event) => setScheduledAt(event.target.value)} />
          </label>
          <label>
            Duração (minutos)
            <input
              className="input"
              type="number"
              min={1}
              max={240}
              value={durationMinutes}
              onChange={(event) => setDurationMinutes(Number(event.target.value))}
            />
          </label>
          <label>
            Link da reunião (opcional)
            <input className="input" value={meetingUrl} onChange={(event) => setMeetingUrl(event.target.value)} />
          </label>
          <label>
            Observações
            <textarea className="input" rows={3} maxLength={2000} value={notes} onChange={(event) => setNotes(event.target.value)} />
          </label>
          <button className="btn" type="submit" disabled={saving}>
            {saving ? "Agendando..." : "Criar sessão"}
          </button>
        </form>
      ) : mentorship.status === "ACTIVE" && mentorship.paymentRequired && !mentorship.paymentSettled ? (
        <p className="post-meta">As sessões são liberadas após a confirmação do pagamento.</p>
      ) : null}

      <section className="enrollment-section">
        <h2>Sessões</h2>
        <AgendaList items={sessions} viewerRole={isMentor ? "MENTOR" : "MENTEE"} onChange={setSessions} />
      </section>

      {completedSessions.length > 0 ? (
        <section className="enrollment-section">
          <h2>Sessões concluídas</h2>
          <ul className="enrollment-list">
            {completedSessions.map((session) => (
              <li key={session.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">{formatSessionDate(session.scheduledAt)}</p>
                  {session.notes ? <p className="post-meta">{session.notes}</p> : null}
                </div>
              </li>
            ))}
          </ul>
        </section>
      ) : null}

      {isMentor && (mentorship.status === "ACTIVE" || mentorship.status === "PAUSED") ? (
        <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem", marginTop: "1.5rem" }}>
          {!mentorship.canComplete ? (
            <p className="post-meta">
              Para concluir: pagamento quitado, {mentorship.requiredSessions} sessão(ões) concluída(s) e nenhuma sessão agendada.
            </p>
          ) : null}
          <div style={{ display: "flex", gap: "0.75rem" }}>
            <button className="btn" type="button" disabled={!mentorship.canComplete} onClick={() => void onCompleteMentorship()}>
              Concluir mentoria
            </button>
            <button className="btn secondary" type="button" onClick={() => void onCancelMentorship()}>
              Cancelar mentoria
            </button>
          </div>
        </div>
      ) : null}

      {mentorship.status === "COMPLETED" ? (
        <section className="enrollment-section">
          <h2>Histórico</h2>
          <p className="post-meta">
            Concluída em {mentorship.completedAt ? formatSessionDate(mentorship.completedAt) : "—"}.
          </p>
          <p>
            <Link className="text-btn" href={`/mentors/${mentorship.mentorProfileId}`}>
              Contratar nova mentoria
            </Link>
          </p>
          {isParticipant ? (
            <form className="card-form" onSubmit={onReview} style={{ display: "grid", gap: "0.75rem", marginTop: "1rem" }}>
              <h3 className="help-heading">
                {isMentee ? "Avaliar mentor" : "Avaliar mentorado"}
                <HelpTooltip
                  text="Dê uma nota de 1 a 5. Você pode editar a avaliação por até 72 horas."
                  href="/ajuda/avaliacoes"
                  label="Ajuda sobre avaliações"
                />
              </h3>
              <label>
                Nota
                <select className="input" value={rating} onChange={(event) => setRating(Number(event.target.value))}>
                  {[1, 2, 3, 4, 5].map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Comentário (opcional)
                <textarea className="input" rows={3} maxLength={2000} value={comment} onChange={(event) => setComment(event.target.value)} />
              </label>
              <button className="btn" type="submit">
                {myReview ? "Atualizar avaliação" : "Enviar avaliação"}
              </button>
              {myReview ? <p className="post-meta">Você pode editar a avaliação por até 72 horas.</p> : null}
            </form>
          ) : null}
          {reviews.length > 0 ? (
            <ul className="enrollment-list" style={{ marginTop: "1rem" }}>
              {reviews.map((review) => (
                <li key={review.id} className="enrollment-item">
                  <div>
                    <p className="enrollment-title">
                      {review.reviewerName} • {review.rating}/5
                    </p>
                    {review.comment ? <p className="post-meta">{review.comment}</p> : null}
                  </div>
                </li>
              ))}
            </ul>
          ) : null}
        </section>
      ) : null}
    </main>
  );
}
