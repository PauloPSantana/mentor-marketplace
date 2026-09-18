"use client";

import { FormEvent, useEffect, useState } from "react";
import { AnnouncementCard } from "@/components/groups/AnnouncementCard";
import { apiErrorMessage } from "@/lib/api";
import {
  createAnnouncement,
  listAnnouncements,
  type Announcement,
  type AnnouncementAudienceType
} from "@/lib/announcements";
import type { GroupMember, MentorshipGroup } from "@/lib/groups";

type GroupBoardProps = {
  group: MentorshipGroup;
  currentUserId?: string;
  isMentor: boolean;
};

export function GroupBoard({ group, currentUserId, isMentor }: GroupBoardProps) {
  const [items, setItems] = useState<Announcement[]>([]);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [audienceType, setAudienceType] = useState<AnnouncementAudienceType>("GROUP");
  const [selected, setSelected] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const canInteract = group.status === "ACTIVE";
  const mentees = group.members.filter((member) => member.role !== "MENTOR");

  useEffect(() => {
    setLoading(true);
    listAnnouncements(group.id)
      .then(setItems)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o mural.")))
      .finally(() => setLoading(false));
  }, [group.id]);

  function toggleRecipient(userId: string) {
    setSelected((current) =>
      current.includes(userId) ? current.filter((id) => id !== userId) : [...current, userId]
    );
  }

  async function onPublish(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const created = await createAnnouncement(group.id, {
        title,
        body,
        audienceType,
        recipientUserIds: audienceType === "MEMBERS" ? selected : []
      });
      setItems((current) => [created, ...current]);
      setTitle("");
      setBody("");
      setSelected([]);
      setAudienceType("GROUP");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível publicar no mural."));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="enrollment-section">
      <h2>Mural do grupo</h2>
      <p className="post-meta">
        Comunicados da mentoria: o mentor publica para toda a turma ou para mentorados escolhidos.
      </p>
      {error ? <p className="error">{error}</p> : null}

      {isMentor && canInteract ? (
        <form className="card-form" onSubmit={onPublish} style={{ display: "grid", gap: "0.75rem", margin: "1rem 0" }}>
          <label>
            Título (opcional)
            <input
              className="input"
              maxLength={180}
              value={title}
              onChange={(event) => setTitle(event.target.value)}
              placeholder="Ex.: Próxima sessão — microsserviços"
            />
          </label>
          <label>
            Comunicado
            <textarea
              className="input"
              required
              rows={6}
              maxLength={5000}
              value={body}
              onChange={(event) => setBody(event.target.value)}
              placeholder={"Pessoal, na próxima sessão vamos trabalhar arquitetura de microsserviços.\n\nMaterial recomendado:\n- SOLID\n- Clean Architecture"}
            />
          </label>
          <fieldset style={{ border: 0, padding: 0 }}>
            <legend className="post-meta">Destinatário</legend>
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.4rem" }}>
              <input
                type="radio"
                name="audience"
                checked={audienceType === "GROUP"}
                onChange={() => setAudienceType("GROUP")}
              />
              Todo o grupo
            </label>
            <label style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.4rem" }}>
              <input
                type="radio"
                name="audience"
                checked={audienceType === "MEMBERS"}
                onChange={() => setAudienceType("MEMBERS")}
              />
              Mentorados selecionados
            </label>
            {audienceType === "MEMBERS" ? (
              <div style={{ marginTop: "0.5rem" }}>
                {mentees.map((member: GroupMember) => (
                  <label
                    key={member.userId}
                    style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.35rem" }}
                  >
                    <input
                      type="checkbox"
                      checked={selected.includes(member.userId)}
                      onChange={() => toggleRecipient(member.userId)}
                    />
                    {member.name}
                  </label>
                ))}
              </div>
            ) : null}
          </fieldset>
          <button
            className="btn"
            type="submit"
            disabled={saving || !body.trim() || (audienceType === "MEMBERS" && selected.length === 0)}
          >
            {saving ? "Publicando..." : "Publicar no mural"}
          </button>
        </form>
      ) : null}

      {loading ? (
        <p className="post-meta">Carregando mural...</p>
      ) : items.length === 0 ? (
        <p className="feed-empty">Nenhum comunicado ainda.</p>
      ) : (
        <ul className="enrollment-list">
          {items.map((item) => (
            <li key={item.id}>
              <AnnouncementCard
                groupId={group.id}
                announcement={item}
                currentUserId={currentUserId}
                canInteract={canInteract}
                onUpdated={(updated) =>
                  setItems((current) => current.map((entry) => (entry.id === updated.id ? updated : entry)))
                }
              />
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
