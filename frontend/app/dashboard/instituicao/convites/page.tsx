"use client";

import { useEffect, useMemo, useState } from "react";
import { InviteMentorForm } from "@/components/institutions/InviteMentorForm";
import { apiErrorMessage } from "@/lib/api";
import {
  cancelMentorInvitation,
  getInstitutionDashboard,
  invitationStatusClass,
  invitationStatusLabel,
  resendMentorInvitation,
  type InstitutionInvitation
} from "@/lib/institutions";

const FILTERS = ["PENDING", "ACCEPTED", "EXPIRED", "CANCELLED"] as const;

export default function InstitutionInvitesPage() {
  const [invitations, setInvitations] = useState<InstitutionInvitation[]>([]);
  const [institutionName, setInstitutionName] = useState("sua instituição");
  const [filter, setFilter] = useState<(typeof FILTERS)[number]>("PENDING");
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<string | null>(null);

  function load() {
    getInstitutionDashboard()
      .then((dashboard) => {
        setInvitations(dashboard.invitations);
        setInstitutionName(dashboard.name);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os convites.")));
  }

  useEffect(() => {
    load();
  }, []);

  const visible = useMemo(
    () => invitations.filter((item) => item.status === filter),
    [filter, invitations]
  );

  async function resend(id: string) {
    setBusyId(id);
    setError(null);
    try {
      const updated = await resendMentorInvitation(id);
      setInvitations((current) => current.map((item) => (item.id === id ? updated : item)));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível reenviar o convite."));
    } finally {
      setBusyId(null);
    }
  }

  async function cancel(id: string) {
    setBusyId(id);
    setError(null);
    try {
      await cancelMentorInvitation(id);
      setInvitations((current) => current.map((item) => (
        item.id === id ? { ...item, status: "CANCELLED" } : item
      )));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível cancelar o convite."));
    } finally {
      setBusyId(null);
    }
  }

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Convites</h1>
          <p className="post-meta">Envie, reenvie ou cancele convites de mentores.</p>
        </div>
      </header>

      <section className="enrollment-section">
        <h2>Novo convite</h2>
        <InviteMentorForm
          institutionName={institutionName}
          onCreated={(created) => setInvitations((current) => [created, ...current])}
        />
      </section>

      <section className="enrollment-section">
        <h2>Convites enviados</h2>
        <div className="filter-chips">
          {FILTERS.map((item) => (
            <button
              key={item}
              type="button"
              className={`filter-chip${filter === item ? " active" : ""}`}
              onClick={() => setFilter(item)}
            >
              {invitationStatusLabel(item)}
            </button>
          ))}
        </div>
        {error ? <p className="error">{error}</p> : null}
        {visible.length === 0 ? (
          <p className="feed-empty">Nenhum convite neste status.</p>
        ) : (
          <ul className="enrollment-list">
            {visible.map((invitation) => (
              <li key={invitation.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">{invitation.name}</p>
                  <p className="post-meta">{invitation.email}{invitation.program ? ` · ${invitation.program}` : ""}</p>
                  {invitation.status === "PENDING" ? (
                    <p className="post-meta">
                      Link: <a href={invitation.inviteUrl}>{invitation.inviteUrl}</a>
                    </p>
                  ) : null}
                </div>
                <div className="enrollment-actions">
                  <span className={`status-badge ${invitationStatusClass(invitation.status)}`}>
                    {invitationStatusLabel(invitation.status)}
                  </span>
                  {invitation.status !== "ACCEPTED" ? (
                    <button className="text-btn" type="button" disabled={busyId === invitation.id} onClick={() => void resend(invitation.id)}>
                      Reenviar
                    </button>
                  ) : null}
                  {invitation.status === "PENDING" || invitation.status === "EXPIRED" ? (
                    <button className="text-btn" type="button" disabled={busyId === invitation.id} onClick={() => void cancel(invitation.id)}>
                      Cancelar
                    </button>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
