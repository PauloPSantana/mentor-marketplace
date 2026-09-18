"use client";

import Link from "next/link";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { AgendaList } from "@/components/mentorships/AgendaList";
import { StudyPlanPanel } from "@/components/mentorships/StudyPlanPanel";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { getGoogleStatus } from "@/lib/google";
import { getZoomStatus } from "@/lib/zoom";
import {
  cancelMentorship,
  completeMentorship,
  createMentorshipSession,
  formatSessionDate,
  getMentorshipRelationship,
  listMentorshipSessions,
  meetingProviderLabel,
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
import { bookMentorSlot, getMentorAvailability, type MentorAvailability } from "@/lib/availability";

export default function MentorshipDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const searchParams = useSearchParams();
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
  const [scheduledDate, setScheduledDate] = useState("");
  const [scheduledTime, setScheduledTime] = useState("19:00");
  const [durationMinutes, setDurationMinutes] = useState(60);
  const [sessionTitle, setSessionTitle] = useState("");
  const [meetingProvider, setMeetingProvider] = useState<"GOOGLE_MEET" | "ZOOM" | "MANUAL">("GOOGLE_MEET");
  const [meetingUrl, setMeetingUrl] = useState("");
  const [recordingConsent, setRecordingConsent] = useState(false);
  const [notes, setNotes] = useState("");
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState("");
  const [lastCreated, setLastCreated] = useState<MentorshipSession | null>(null);
  const [availability, setAvailability] = useState<MentorAvailability | null>(null);
  const [selectedSlot, setSelectedSlot] = useState<string | null>(null);
  const requestedTab = searchParams.get("tab");
  const [tab, setTab] = useState<"visao" | "sessoes" | "plano" | "tarefas" | "questionarios" | "materiais" | "evolucao">(
    requestedTab === "plano" || requestedTab === "tarefas" || requestedTab === "questionarios" || requestedTab === "materiais" || requestedTab === "sessoes" || requestedTab === "evolucao"
      ? requestedTab
      : "visao"
  );

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

  useEffect(() => {
    if (!mentorship) {
      return;
    }
    getMentorAvailability(mentorship.mentorProfileId)
      .then(setAvailability)
      .catch(() => setAvailability(null));
  }, [mentorship]);

  useEffect(() => {
    if (!isMentor) {
      return;
    }
    Promise.all([getGoogleStatus().catch(() => null), getZoomStatus().catch(() => null)]).then(([google, zoom]) => {
      if (google?.connected) {
        setMeetingProvider("GOOGLE_MEET");
      } else if (zoom?.connected || zoom?.accountMeetingsEnabled) {
        setMeetingProvider("ZOOM");
      } else {
        setMeetingProvider("MANUAL");
      }
    });
  }, [isMentor]);

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

  async function onBookSlot(startAt: string) {
    if (!mentorship) return;
    setError(null);
    setSaving(true);
    setSelectedSlot(startAt);
    try {
      const created = await bookMentorSlot(mentorship.mentorProfileId, {
        mentorshipId: mentorship.id,
        startAt
      });
      setSessions((current) => [...current, created].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)));
      setLastCreated(created);
      setSelectedSlot(null);
      const refreshed = await getMentorAvailability(mentorship.mentorProfileId);
      setAvailability(refreshed);
      await refreshRelationship();
    } catch (err) {
      setError(apiErrorMessage(err, "Este horário não está mais disponível."));
      getMentorAvailability(mentorship.mentorProfileId).then(setAvailability).catch(() => undefined);
    } finally {
      setSaving(false);
    }
  }

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!mentorship) return;
    setError(null);
    setSaving(true);
    try {
      const created = await createMentorshipSession(mentorship.id, {
        scheduledAt: new Date(`${scheduledDate}T${scheduledTime}`).toISOString(),
        durationMinutes,
        meetingUrl: meetingProvider === "MANUAL" ? meetingUrl : "",
        notes,
        recordingConsent: meetingProvider === "ZOOM" ? recordingConsent : false,
        meetingProvider: meetingProvider === "MANUAL" ? null : meetingProvider,
        title: sessionTitle || mentorship.serviceName
      });
      setSessions((current) => [...current, created].sort((a, b) => a.scheduledAt.localeCompare(b.scheduledAt)));
      setLastCreated(created);
      setScheduledDate("");
      setMeetingUrl("");
      setRecordingConsent(false);
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
        <Link href={currentUser?.role === "MENTOR" ? "/dashboard/mentor" : "/dashboard/mentorado"}>Minha Mentoria</Link>
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
        <div className="enrollment-section" style={{ marginTop: "1rem" }}>
          <h2>Próxima mentoria</h2>
          <p className="enrollment-title">{mentorship.serviceName}</p>
          <p className="post-meta">{formatSessionDate(mentorship.nextSession.scheduledAt)}</p>
          <p className="post-meta">
            {isMentee ? `Mentor: ${mentorship.mentor.name}` : `Mentorado: ${mentorship.mentee.name}`}
          </p>
          {mentorship.nextSession.meetingProvider ? (
            <p className="post-meta">{meetingProviderLabel(mentorship.nextSession.meetingProvider)}</p>
          ) : null}
          {mentorship.nextSession.meetingUrl && mentorship.nextSession.status === "SCHEDULED" ? (
            <p>
              <a className="btn" href={mentorship.nextSession.meetingUrl} target="_blank" rel="noreferrer">
                Entrar na reunião
              </a>
            </p>
          ) : null}
        </div>
      ) : null}
      {error ? <p className="error">{error}</p> : null}

      <nav className="classroom-tabs" aria-label="Minha Mentoria">
        <button className={`classroom-tab${tab === "visao" ? " active" : ""}`} type="button" onClick={() => setTab("visao")}>
          Visão Geral
        </button>
        <button className={`classroom-tab${tab === "sessoes" ? " active" : ""}`} type="button" onClick={() => setTab("sessoes")}>
          Sessões
        </button>
        <button className={`classroom-tab${tab === "plano" ? " active" : ""}`} type="button" onClick={() => setTab("plano")}>
          Plano de Estudos
        </button>
        <button className={`classroom-tab${tab === "tarefas" ? " active" : ""}`} type="button" onClick={() => setTab("tarefas")}>
          Tarefas
        </button>
        <button className={`classroom-tab${tab === "questionarios" ? " active" : ""}`} type="button" onClick={() => setTab("questionarios")}>
          Questionários
        </button>
        <button className={`classroom-tab${tab === "materiais" ? " active" : ""}`} type="button" onClick={() => setTab("materiais")}>
          Materiais
        </button>
        <button className={`classroom-tab${tab === "evolucao" ? " active" : ""}`} type="button" onClick={() => setTab("evolucao")}>
          Evolução
        </button>
      </nav>

      {tab === "plano" ? (
        <StudyPlanPanel
          mentorshipId={mentorship.id}
          menteeName={mentorship.mentee.name}
          isMentor={Boolean(isMentor)}
          isMentee={Boolean(isMentee)}
        />
      ) : null}
      {tab === "tarefas" ? (
        <StudyPlanPanel
          mentorshipId={mentorship.id}
          menteeName={mentorship.mentee.name}
          isMentor={Boolean(isMentor)}
          isMentee={Boolean(isMentee)}
          mode="tasks"
        />
      ) : null}
      {tab === "materiais" ? (
        <StudyPlanPanel
          mentorshipId={mentorship.id}
          menteeName={mentorship.mentee.name}
          isMentor={Boolean(isMentor)}
          isMentee={Boolean(isMentee)}
          mode="materials"
        />
      ) : null}
      {tab === "evolucao" ? (
        <StudyPlanPanel
          mentorshipId={mentorship.id}
          menteeName={mentorship.mentee.name}
          isMentor={Boolean(isMentor)}
          isMentee={Boolean(isMentee)}
          mode="progress"
        />
      ) : null}
      {tab === "questionarios" ? (
        <section className="enrollment-section">
          <h2>Questionários</h2>
          <p className="post-meta">Em breve: diagnósticos, perguntas e respostas do mentorado.</p>
        </section>
      ) : null}

      {tab === "visao" || tab === "sessoes" ? (
        <>
      {tab === "visao" ? (
        <p className="post-meta">
          Use o Plano de Estudos para montar a trilha. O mentorado marca o que já concluiu e o progresso aparece em Evolução.
        </p>
      ) : null}
      {tab === "visao" && mentorship.paymentRequired ? (
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

      {tab === "sessoes" && canSchedule && isMentee ? (
        <section className="enrollment-section" style={{ margin: "1.5rem 0" }}>
          <h2>Calendário</h2>
          <p className="post-meta">Escolha um horário livre. Compromissos pessoais do mentor não aparecem.</p>
          {availability && availability.days.length > 0 ? (
            availability.days.map((day) => (
              <div key={day.date} style={{ marginTop: "1rem" }}>
                <p className="enrollment-title">
                  {new Intl.DateTimeFormat("pt-BR", { day: "2-digit", month: "2-digit" }).format(new Date(`${day.date}T12:00:00`))}
                </p>
                {day.slots.length === 0 ? (
                  <p className="post-meta">Sem disponibilidade</p>
                ) : (
                  <div style={{ display: "flex", gap: "0.5rem", flexWrap: "wrap" }}>
                    {day.slots.map((slot) => (
                      <button
                        key={slot.startAt}
                        className="btn secondary"
                        type="button"
                        disabled={saving}
                        onClick={() => void onBookSlot(slot.startAt)}
                      >
                        {selectedSlot === slot.startAt && saving ? "Reservando..." : slot.label}
                      </button>
                    ))}
                  </div>
                )}
              </div>
            ))
          ) : (
            <p className="post-meta">O mentor ainda não definiu horários disponíveis.</p>
          )}
        </section>
      ) : null}

      {tab === "sessoes" && canSchedule && isMentor ? (
        <form className="card-form" onSubmit={onCreate} style={{ display: "grid", gap: "0.75rem", margin: "1.5rem 0" }}>
          <h2>Nova sessão</h2>
          <p className="post-meta">Mentorado: {mentorship.mentee.name}</p>
          <label>
            Título
            <input
              className="input"
              value={sessionTitle}
              onChange={(event) => setSessionTitle(event.target.value)}
              placeholder={mentorship.serviceName}
            />
          </label>
          <label>
            Data
            <input className="input" type="date" required value={scheduledDate} onChange={(event) => setScheduledDate(event.target.value)} />
          </label>
          <label>
            Horário
            <input className="input" type="time" required value={scheduledTime} onChange={(event) => setScheduledTime(event.target.value)} />
          </label>
          <label>
            Duração
            <input
              className="input"
              type="number"
              min={1}
              max={240}
              value={durationMinutes}
              onChange={(event) => setDurationMinutes(Number(event.target.value))}
            />
            <span className="post-meta">minutos</span>
          </label>
          <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
            <legend className="post-meta">Videoconferência</legend>
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <input
                type="radio"
                name="meetingProvider"
                checked={meetingProvider === "GOOGLE_MEET"}
                onChange={() => setMeetingProvider("GOOGLE_MEET")}
              />
              Google Meet
            </label>
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <input
                type="radio"
                name="meetingProvider"
                checked={meetingProvider === "ZOOM"}
                onChange={() => setMeetingProvider("ZOOM")}
              />
              Zoom
            </label>
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <input
                type="radio"
                name="meetingProvider"
                checked={meetingProvider === "MANUAL"}
                onChange={() => setMeetingProvider("MANUAL")}
              />
              Link manual / presencial
            </label>
          </fieldset>
          {meetingProvider === "MANUAL" ? (
            <label>
              Link da reunião (opcional)
              <input
                className="input"
                value={meetingUrl}
                onChange={(event) => setMeetingUrl(event.target.value)}
                placeholder="Cole o link se a reunião for externa"
              />
            </label>
          ) : null}
          {meetingProvider === "ZOOM" ? (
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
              <input type="checkbox" checked={recordingConsent} onChange={(event) => setRecordingConsent(event.target.checked)} />
              Autorizo gravação da sessão no Zoom, se o plano permitir
            </label>
          ) : null}
          <label>
            Observações
            <textarea className="input" rows={3} maxLength={2000} value={notes} onChange={(event) => setNotes(event.target.value)} />
          </label>
          <button className="btn" type="submit" disabled={saving}>
            {saving ? "Agendando..." : "Agendar"}
          </button>
        </form>
      ) : tab === "sessoes" && mentorship.status === "ACTIVE" && mentorship.paymentRequired && !mentorship.paymentSettled ? (
        <p className="post-meta">As sessões são liberadas após a confirmação do pagamento.</p>
      ) : null}

      {tab === "sessoes" && lastCreated ? (
        <section className="enrollment-section">
          <h2>Sessão agendada com sucesso</h2>
          {lastCreated.meetingProvider === "GOOGLE_MEET" ? (
            <>
              <p className="post-meta">Google Agenda: {lastCreated.googleCalendarCreated ? "criado" : "não criado"}</p>
              <p className="post-meta">Google Meet: {lastCreated.googleMeetCreated ? "criado" : "não criado"}</p>
            </>
          ) : lastCreated.meetingProvider === "ZOOM" ? (
            <p className="post-meta">Zoom: reunião criada</p>
          ) : (
            <p className="post-meta">Sessão registrada na agenda do Mentor Marketplace.</p>
          )}
          {lastCreated.meetingUrl && lastCreated.status === "SCHEDULED" ? (
            <p>
              <a className="btn" href={lastCreated.meetingUrl} target="_blank" rel="noreferrer">
                Entrar na reunião
              </a>
            </p>
          ) : null}
        </section>
      ) : null}

      {tab === "sessoes" ? (
      <>
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
      </>
      ) : null}

      {tab === "visao" && isMentor && (mentorship.status === "ACTIVE" || mentorship.status === "PAUSED") ? (
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

      {tab === "visao" && mentorship.status === "COMPLETED" && isParticipant ? (
        <p className="post-meta" style={{ marginTop: "1rem" }}>
          Defina a próxima meta na nova mentoria ou avalie a sessão abaixo.
        </p>
      ) : null}

      {tab === "visao" && mentorship.status === "COMPLETED" ? (
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
        </>
      ) : null}
    </main>
  );
}
