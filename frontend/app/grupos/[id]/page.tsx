"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { GroupBoard } from "@/components/groups/GroupBoard";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import {
  addGroupMember,
  closeGroup,
  getGroup,
  groupStatusLabel,
  listGroupAddCandidates,
  removeGroupMember,
  type GroupCandidate,
  type MentorshipGroup
} from "@/lib/groups";

type ClassroomTab = "mural" | "tarefas" | "questionarios" | "materiais";

export default function GroupDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const currentUser = getStoredUser();
  const [group, setGroup] = useState<MentorshipGroup | null>(null);
  const [candidates, setCandidates] = useState<GroupCandidate[]>([]);
  const [mentorshipId, setMentorshipId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState<ClassroomTab>("mural");

  useEffect(() => {
    if (!currentUser) {
      router.replace("/login");
      return;
    }
    Promise.all([getGroup(params.id), listGroupAddCandidates(params.id).catch(() => [] as GroupCandidate[])])
      .then(([item, people]) => {
        setGroup(item);
        setCandidates(people);
        setMentorshipId(people[0]?.mentorshipId ?? "");
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o grupo.")))
      .finally(() => setLoading(false));
  }, [params.id, router, currentUser?.id]);

  async function onAdd(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!group) return;
    setError(null);
    try {
      const updated = await addGroupMember(group.id, mentorshipId);
      setGroup(updated);
      setCandidates(await listGroupAddCandidates(group.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível adicionar ao grupo."));
    }
  }

  async function onRemove(userId: string) {
    if (!group) return;
    setError(null);
    try {
      setGroup(await removeGroupMember(group.id, userId));
      setCandidates(await listGroupAddCandidates(group.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar o grupo."));
    }
  }

  async function onClose() {
    if (!group) return;
    setError(null);
    try {
      setGroup(await closeGroup(group.id));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível encerrar o grupo."));
    }
  }

  if (loading) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  if (!group) {
    return (
      <main className="container" style={{ padding: "3rem 0" }}>
        <p className="error">{error ?? "Grupo não encontrado."}</p>
      </main>
    );
  }

  const canManage = group.status === "ACTIVE" && (group.owner || group.mentor);
  const isMentor = Boolean(group.mentor) || currentUser?.id === group.mentorUserId || currentUser?.role === "MENTOR";

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 760 }}>
      <p className="post-meta">
        <Link href="/grupos">Grupos</Link>
      </p>
      <h1>{group.title}</h1>
      <p>
        Sala da mentoria · Mentor: {group.mentorName}.{" "}
        <span className={`status-badge ${group.status.toLowerCase()}`}>{groupStatusLabel(group.status)}</span>
      </p>
      {group.productTitle ? <p className="post-meta">{group.productTitle}</p> : null}
      {group.description ? <p>{group.description}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <nav className="classroom-tabs" aria-label="Espaço da mentoria">
        <button className={`classroom-tab${tab === "mural" ? " active" : ""}`} type="button" onClick={() => setTab("mural")}>
          Mural
        </button>
        <button className={`classroom-tab${tab === "tarefas" ? " active" : ""}`} type="button" onClick={() => setTab("tarefas")}>
          Tarefas
        </button>
        <button
          className={`classroom-tab${tab === "questionarios" ? " active" : ""}`}
          type="button"
          onClick={() => setTab("questionarios")}
        >
          Questionários
        </button>
        <button
          className={`classroom-tab${tab === "materiais" ? " active" : ""}`}
          type="button"
          onClick={() => setTab("materiais")}
        >
          Materiais
        </button>
      </nav>

      {tab === "mural" ? (
        <GroupBoard group={group} currentUserId={currentUser?.id} isMentor={Boolean(isMentor)} />
      ) : (
        <section className="enrollment-section">
          <h2>{tabLabel(tab)}</h2>
          <p className="post-meta">{comingSoonCopy(tab)}</p>
          {tab === "tarefas" || tab === "questionarios" || tab === "materiais" ? (
            <ul className="enrollment-list" style={{ marginTop: "1rem" }}>
              {group.members
                .filter((member) => member.role !== "MENTOR" && member.mentorshipId)
                .map((member) => (
                  <li key={member.id} className="enrollment-item">
                    <div>
                      <p className="enrollment-title">{member.name}</p>
                      <p className="post-meta">Abrir a mentoria para cadastrar {tabLabel(tab).toLowerCase()}</p>
                    </div>
                    <Link className="btn secondary" href={`/dashboard/mentorships/${member.mentorshipId}?tab=${tab === "questionarios" ? "questionarios" : tab === "materiais" ? "materiais" : "plano"}`}>
                      Minha Mentoria
                    </Link>
                  </li>
                ))}
            </ul>
          ) : null}
        </section>
      )}

      <section className="enrollment-section">
        <h2>Integrantes</h2>
        <ul className="enrollment-list">
          {group.members.map((member) => (
            <li key={member.id} className="enrollment-item">
              <div>
                <p className="enrollment-title">{member.name}</p>
                <p className="post-meta">{member.role === "MENTOR" ? "Mentor" : "Mentorado"}</p>
              </div>
              {group.status === "ACTIVE" && member.role !== "MENTOR" && (canManage || member.userId === currentUser?.id) ? (
                <button className="btn secondary" type="button" onClick={() => void onRemove(member.userId)}>
                  {member.userId === currentUser?.id ? "Sair" : "Remover"}
                </button>
              ) : null}
            </li>
          ))}
        </ul>
      </section>

      {canManage && candidates.length > 0 ? (
        <form className="card-form" onSubmit={onAdd} style={{ display: "grid", gap: "0.75rem", marginTop: "1.5rem" }}>
          <h2>Adicionar mentorado</h2>
          <label>
            Mentoria ativa
            <select className="input" value={mentorshipId} onChange={(event) => setMentorshipId(event.target.value)}>
              {candidates.map((item) => (
                <option key={item.mentorshipId} value={item.mentorshipId}>
                  {item.name} · {item.serviceName}
                </option>
              ))}
            </select>
          </label>
          <button className="btn" type="submit">
            Adicionar
          </button>
        </form>
      ) : null}

      {canManage ? (
        <button className="btn secondary" type="button" style={{ marginTop: "1.5rem" }} onClick={() => void onClose()}>
          Encerrar grupo
        </button>
      ) : null}
    </main>
  );
}

function tabLabel(tab: ClassroomTab): string {
  switch (tab) {
    case "tarefas":
      return "Tarefas";
    case "questionarios":
      return "Questionários";
    case "materiais":
      return "Materiais";
    default:
      return "Mural";
  }
}

function comingSoonCopy(tab: ClassroomTab): string {
  switch (tab) {
    case "tarefas":
      return "As tarefas são cadastradas na mentoria de cada mentorado, em Plano de Estudos.";
    case "questionarios":
      return "O cadastro de questionários ainda não está nesta tela. Entre na mentoria do mentorado; a aba Questionários entra no próximo sprint.";
    case "materiais":
      return "Em breve: o mentor poderá anexar arquivos e links de estudo.";
    default:
      return "";
  }
}
