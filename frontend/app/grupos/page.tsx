"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  createGroup,
  groupStatusLabel,
  listGroupCandidates,
  listGroups,
  type GroupCandidate,
  type MentorshipGroup
} from "@/lib/groups";

export default function GroupsPage() {
  const router = useRouter();
  const currentUser = getStoredUser();
  const [groups, setGroups] = useState<MentorshipGroup[]>([]);
  const [candidates, setCandidates] = useState<GroupCandidate[]>([]);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [selected, setSelected] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!currentUser) {
      router.replace("/login");
      return;
    }
    Promise.all([listGroups(), listGroupCandidates()])
      .then(([items, people]) => {
        setGroups(items);
        setCandidates(people);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os grupos.")))
      .finally(() => setLoading(false));
  }, [router, currentUser?.id]);

  function toggle(mentorshipId: string) {
    setSelected((current) =>
      current.includes(mentorshipId) ? current.filter((id) => id !== mentorshipId) : [...current, mentorshipId]
    );
  }

  async function onCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const created = await createGroup({ title, description, mentorshipIds: selected });
      setGroups((current) => [created, ...current]);
      setTitle("");
      setDescription("");
      setSelected([]);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível criar o grupo."));
    } finally {
      setSaving(false);
    }
  }

  if (!currentUser || loading) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 760 }}>
      <h1>Grupos de mentoria</h1>
      <p className="post-meta">
        Mentor e mentorados de mentorias ativas podem formar uma turma. Dentro do grupo, o mentor publica no mural, e em seguida virão tarefas e questionários.
      </p>
      {error ? <p className="error">{error}</p> : null}

      <form className="card-form" onSubmit={onCreate} style={{ display: "grid", gap: "0.75rem", margin: "1.5rem 0" }}>
        <h2>Criar grupo</h2>
        <label>
          Nome do grupo
          <input className="input" required maxLength={180} value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <label>
          Descrição (opcional)
          <textarea className="input" rows={3} maxLength={2000} value={description} onChange={(event) => setDescription(event.target.value)} />
        </label>
        <fieldset style={{ border: 0, padding: 0 }}>
          <legend className="post-meta">Quem entra no grupo</legend>
          {candidates.length === 0 ? (
            <p className="post-meta">Você precisa de uma mentoria ativa para formar um grupo.</p>
          ) : (
            candidates.map((item) => (
              <label key={item.mentorshipId} style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginTop: "0.4rem" }}>
                <input
                  type="checkbox"
                  checked={selected.includes(item.mentorshipId)}
                  onChange={() => toggle(item.mentorshipId)}
                />
                {item.name} · {item.serviceName} ({item.role === "MENTOR" ? "mentor" : "mentorado"})
              </label>
            ))
          )}
        </fieldset>
        <button className="btn" type="submit" disabled={saving || selected.length === 0}>
          {saving ? "Criando..." : "Criar grupo"}
        </button>
      </form>

      <section className="enrollment-section">
        <h2>Meus grupos</h2>
        {groups.length === 0 ? (
          <p className="feed-empty">Nenhum grupo ainda.</p>
        ) : (
          <ul className="enrollment-list">
            {groups.map((group) => (
              <li key={group.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">{group.title}</p>
                  <p className="post-meta">
                    Mentor: {group.mentorName} · {group.memberCount} integrante(s)
                    {group.productTitle ? ` · ${group.productTitle}` : ""}
                  </p>
                </div>
                <div className="enrollment-actions">
                  <span className={`status-badge ${group.status.toLowerCase()}`}>{groupStatusLabel(group.status)}</span>
                  <Link className="btn secondary" href={`/grupos/${group.id}`}>
                    Abrir
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
