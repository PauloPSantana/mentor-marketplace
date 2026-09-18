import { api, ApiError } from "@/lib/api";

export type StudyTaskType =
  | "VIDEO"
  | "ARTICLE"
  | "BOOK"
  | "COURSE"
  | "EXERCISE"
  | "PROJECT"
  | "QUIZ"
  | "OTHER";

export type StudyTaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export type StudyTask = {
  id: string;
  title: string;
  description: string | null;
  taskType: StudyTaskType;
  orderNumber: number;
  dueDate: string | null;
  required: boolean;
  resourceTitle: string | null;
  resourceUrl: string | null;
  status: StudyTaskStatus;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
};

export type StudyPlan = {
  id: string;
  mentorshipId: string;
  title: string;
  description: string | null;
  completedCount: number;
  totalCount: number;
  progressPercent: number;
  tasks: StudyTask[];
  createdAt: string;
  updatedAt: string;
};

export type UpsertStudyTaskInput = {
  title: string;
  description?: string;
  taskType: StudyTaskType;
  orderNumber?: number;
  dueDate?: string;
  required: boolean;
  resourceTitle?: string;
  resourceUrl?: string;
};

export async function getStudyPlan(mentorshipId: string): Promise<StudyPlan | null> {
  try {
    const plan = await api<StudyPlan | undefined>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan`);
    return plan ?? null;
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      return null;
    }
    throw error;
  }
}

export function upsertStudyPlan(mentorshipId: string, title: string, description?: string): Promise<StudyPlan> {
  return api<StudyPlan>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan`, {
    method: "PUT",
    body: JSON.stringify({ title, description: description?.trim() ? description.trim() : null })
  });
}

export function createStudyTask(mentorshipId: string, input: UpsertStudyTaskInput): Promise<StudyPlan> {
  return api<StudyPlan>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan/tasks`, {
    method: "POST",
    body: JSON.stringify(toTaskBody(input))
  });
}

export function updateStudyTask(mentorshipId: string, taskId: string, input: UpsertStudyTaskInput): Promise<StudyPlan> {
  return api<StudyPlan>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan/tasks/${taskId}`, {
    method: "PATCH",
    body: JSON.stringify(toTaskBody(input))
  });
}

export function deleteStudyTask(mentorshipId: string, taskId: string): Promise<StudyPlan> {
  return api<StudyPlan>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan/tasks/${taskId}`, {
    method: "DELETE"
  });
}

export function updateStudyTaskProgress(
  mentorshipId: string,
  taskId: string,
  status: StudyTaskStatus
): Promise<StudyPlan> {
  return api<StudyPlan>(`/api/v1/mentorships/relationships/${mentorshipId}/study-plan/tasks/${taskId}/progress`, {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
}

export function studyTaskTypeLabel(type: StudyTaskType): string {
  switch (type) {
    case "VIDEO":
      return "Vídeo";
    case "ARTICLE":
      return "Artigo";
    case "BOOK":
      return "Livro";
    case "COURSE":
      return "Curso";
    case "EXERCISE":
      return "Exercício";
    case "PROJECT":
      return "Projeto";
    case "QUIZ":
      return "Questionário";
    default:
      return "Outro";
  }
}

export function studyTaskStatusLabel(status: StudyTaskStatus): string {
  switch (status) {
    case "IN_PROGRESS":
      return "Em andamento";
    case "COMPLETED":
      return "Concluído";
    default:
      return "Pendente";
  }
}

function toTaskBody(input: UpsertStudyTaskInput) {
  return {
    title: input.title,
    description: input.description?.trim() ? input.description.trim() : null,
    taskType: input.taskType,
    orderNumber: input.orderNumber ?? null,
    dueDate: input.dueDate || null,
    required: input.required,
    resourceTitle: input.resourceTitle?.trim() ? input.resourceTitle.trim() : null,
    resourceUrl: input.resourceUrl?.trim() ? input.resourceUrl.trim() : null
  };
}
