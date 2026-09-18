"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  createStudyTask,
  deleteStudyTask,
  getStudyPlan,
  studyTaskStatusLabel,
  studyTaskTypeLabel,
  updateStudyTaskProgress,
  upsertStudyPlan,
  type StudyPlan,
  type StudyTask,
  type StudyTaskType
} from "@/lib/studyPlans";

type StudyPlanPanelProps = {
  mentorshipId: string;
  menteeName: string;
  isMentor: boolean;
  isMentee: boolean;
  mode?: "full" | "tasks" | "materials" | "progress";
};

const TASK_TYPES: StudyTaskType[] = [
  "VIDEO",
  "ARTICLE",
  "BOOK",
  "COURSE",
  "EXERCISE",
  "PROJECT",
  "QUIZ",
  "OTHER"
];

export function StudyPlanPanel({
  mentorshipId,
  menteeName,
  isMentor,
  isMentee,
  mode = "full"
}: StudyPlanPanelProps) {
  const [plan, setPlan] = useState<StudyPlan | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [taskTitle, setTaskTitle] = useState("");
  const [taskDescription, setTaskDescription] = useState("");
  const [taskType, setTaskType] = useState<StudyTaskType>("VIDEO");
  const [dueDate, setDueDate] = useState("");
  const [required, setRequired] = useState(true);
  const [resourceTitle, setResourceTitle] = useState("");
  const [resourceUrl, setResourceUrl] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setLoading(true);
    getStudyPlan(mentorshipId)
      .then((item) => {
        setPlan(item);
        setTitle(item?.title ?? "");
        setDescription(item?.description ?? "");
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o plano de estudos.")))
      .finally(() => setLoading(false));
  }, [mentorshipId]);

  const visibleTasks = useMemo(() => {
    const tasks = plan?.tasks ?? [];
    if (mode === "materials") {
      return tasks.filter((task) => Boolean(task.resourceUrl));
    }
    return tasks;
  }, [plan, mode]);

  async function onSavePlan(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const saved = await upsertStudyPlan(mentorshipId, title, description);
      setPlan(saved);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível salvar o plano de estudos."));
    } finally {
      setSaving(false);
    }
  }

  async function onCreateTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const saved = await createStudyTask(mentorshipId, {
        title: taskTitle,
        description: taskDescription,
        taskType,
        dueDate,
        required,
        resourceTitle,
        resourceUrl
      });
      setPlan(saved);
      setTaskTitle("");
      setTaskDescription("");
      setDueDate("");
      setRequired(true);
      setResourceTitle("");
      setResourceUrl("");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível adicionar a atividade."));
    } finally {
      setSaving(false);
    }
  }

  async function onToggle(task: StudyTask) {
    if (!isMentee) {
      return;
    }
    setError(null);
    try {
      setPlan(await updateStudyTaskProgress(
        mentorshipId,
        task.id,
        task.status === "COMPLETED" ? "PENDING" : "COMPLETED"
      ));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar a atividade."));
    }
  }

  async function onDelete(taskId: string) {
    if (!window.confirm("Remover esta atividade?")) {
      return;
    }
    setError(null);
    try {
      setPlan(await deleteStudyTask(mentorshipId, taskId));
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível remover a atividade."));
    }
  }

  if (loading) {
    return <p className="post-meta">Carregando plano de estudos...</p>;
  }

  return (
    <section className="enrollment-section">
      <h2>{mode === "progress" ? "Evolução" : mode === "materials" ? "Materiais" : mode === "tasks" ? "Tarefas" : "Plano de Estudos"}</h2>
      {error ? <p className="error">{error}</p> : null}

      {plan ? (
        <>
          {mode !== "materials" ? (
            <>
              <p className="enrollment-title">{plan.title}</p>
              {plan.description ? <p className="post-meta">{plan.description}</p> : null}
              <ProgressBar percent={plan.progressPercent} completed={plan.completedCount} total={plan.totalCount} menteeName={menteeName} />
            </>
          ) : null}

          {visibleTasks.length === 0 ? (
            <p className="feed-empty">
              {mode === "materials" ? "Nenhum material com link ainda." : "Nenhuma atividade ainda."}
            </p>
          ) : (
            <ul className="enrollment-list">
              {visibleTasks.map((task) => (
                <li key={task.id} className="enrollment-item">
                  <div>
                    <p className="enrollment-title">
                      {task.status === "COMPLETED" ? "✓ " : "□ "}
                      {task.orderNumber}. {task.title}
                    </p>
                    <p className="post-meta">
                      {studyTaskTypeLabel(task.taskType)} · {studyTaskStatusLabel(task.status)}
                      {task.required ? " · Obrigatória" : " · Opcional"}
                      {task.dueDate ? ` · Prazo ${formatDueDate(task.dueDate)}` : ""}
                    </p>
                    {task.description ? <p className="post-meta">{task.description}</p> : null}
                    {task.resourceUrl ? (
                      <p className="post-meta">
                        <a href={task.resourceUrl} target="_blank" rel="noreferrer">
                          {task.resourceTitle || "Abrir material"}
                        </a>
                      </p>
                    ) : null}
                  </div>
                  <div className="enrollment-actions">
                    {isMentee ? (
                      <button className="btn secondary" type="button" onClick={() => void onToggle(task)}>
                        {task.status === "COMPLETED" ? "Reabrir" : "Concluir atividade"}
                      </button>
                    ) : (
                      <span className={`status-badge ${task.status === "COMPLETED" ? "completed" : "pending"}`}>
                        {studyTaskStatusLabel(task.status)}
                      </span>
                    )}
                    {isMentor && mode !== "progress" ? (
                      <button className="text-btn danger" type="button" onClick={() => void onDelete(task.id)}>
                        Remover
                      </button>
                    ) : null}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </>
      ) : (
        <p className="feed-empty">
          {isMentor
            ? "Crie o plano de estudos para montar a trilha do mentorado."
            : "O mentor ainda não publicou um plano de estudos."}
        </p>
      )}

      {isMentor && (mode === "full" || mode === "progress") ? (
        <form className="card-form" onSubmit={onSavePlan} style={{ display: "grid", gap: "0.75rem", marginTop: "1.25rem" }}>
          <h3>{plan ? "Editar plano" : "Criar plano de estudos"}</h3>
          <label>
            Título
            <input className="input" required maxLength={180} value={title} onChange={(event) => setTitle(event.target.value)} />
          </label>
          <label>
            Descrição (opcional)
            <textarea className="input" rows={3} maxLength={2000} value={description} onChange={(event) => setDescription(event.target.value)} />
          </label>
          <button className="btn" type="submit" disabled={saving}>
            {saving ? "Salvando..." : "Salvar plano"}
          </button>
        </form>
      ) : null}

      {isMentor && plan && (mode === "full" || mode === "tasks") ? (
        <form className="card-form" onSubmit={onCreateTask} style={{ display: "grid", gap: "0.75rem", marginTop: "1.25rem" }}>
          <h3>Nova atividade</h3>
          <label>
            Título
            <input className="input" required maxLength={180} value={taskTitle} onChange={(event) => setTaskTitle(event.target.value)} />
          </label>
          <label>
            Descrição
            <textarea className="input" rows={3} maxLength={2000} value={taskDescription} onChange={(event) => setTaskDescription(event.target.value)} />
          </label>
          <label>
            Tipo
            <select className="input" value={taskType} onChange={(event) => setTaskType(event.target.value as StudyTaskType)}>
              {TASK_TYPES.map((type) => (
                <option key={type} value={type}>
                  {studyTaskTypeLabel(type)}
                </option>
              ))}
            </select>
          </label>
          <label>
            Prazo (opcional)
            <input className="input" type="date" value={dueDate} onChange={(event) => setDueDate(event.target.value)} />
          </label>
          <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
            <input type="checkbox" checked={required} onChange={(event) => setRequired(event.target.checked)} />
            Atividade obrigatória
          </label>
          <label>
            Título do material (opcional)
            <input className="input" maxLength={180} value={resourceTitle} onChange={(event) => setResourceTitle(event.target.value)} />
          </label>
          <label>
            Link do material (opcional)
            <input className="input" value={resourceUrl} onChange={(event) => setResourceUrl(event.target.value)} placeholder="https://" />
          </label>
          <button className="btn" type="submit" disabled={saving || !taskTitle.trim()}>
            {saving ? "Adicionando..." : "Adicionar atividade"}
          </button>
        </form>
      ) : null}
    </section>
  );
}

function ProgressBar({
  percent,
  completed,
  total,
  menteeName
}: {
  percent: number;
  completed: number;
  total: number;
  menteeName: string;
}) {
  return (
    <div className="study-progress-wrap">
      <p className="post-meta">
        {menteeName}: {completed}/{total} atividades · {percent}%
      </p>
      <div className="study-progress" aria-label={`Progresso ${percent}%`}>
        <div className="study-progress-bar" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}

function formatDueDate(value: string): string {
  const [year, month, day] = value.split("-");
  if (!year || !month || !day) {
    return value;
  }
  return `${day}/${month}/${year}`;
}
