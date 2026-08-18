import { api } from "@/lib/api";

export type MentorshipStatus = "ACTIVE" | "PAUSED" | "COMPLETED" | "CANCELLED";
export type MentorshipSessionStatus = "SCHEDULED" | "COMPLETED" | "CANCELLED" | "NO_SHOW";

export type ParticipantSummary = {
  id: string;
  name: string;
  photoUrl: string | null;
};

export type MentorshipSession = {
  id: string;
  sessionId: string;
  mentorshipId: string;
  scheduledAt: string;
  endsAt: string;
  durationMinutes: number;
  meetingUrl: string | null;
  status: MentorshipSessionStatus;
  notes: string | null;
  cancelReason: string | null;
  participant: ParticipantSummary;
};

export type MentorshipRelationship = {
  id: string;
  requestId: string;
  mentoringServiceId: string;
  serviceName: string;
  status: MentorshipStatus;
  startedAt: string;
  pausedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  mentor: ParticipantSummary;
  mentee: ParticipantSummary;
  nextSession: MentorshipSession | null;
};

export type MentorshipPage = {
  items: MentorshipRelationship[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export function mentorshipStatusLabel(status: MentorshipStatus): string {
  switch (status) {
    case "ACTIVE":
      return "Ativa";
    case "PAUSED":
      return "Pausada";
    case "COMPLETED":
      return "Concluída";
    case "CANCELLED":
      return "Cancelada";
    default:
      return status;
  }
}

export function sessionStatusLabel(status: MentorshipSessionStatus): string {
  switch (status) {
    case "SCHEDULED":
      return "Agendada";
    case "COMPLETED":
      return "Concluída";
    case "CANCELLED":
      return "Cancelada";
    case "NO_SHOW":
      return "Ausência";
    default:
      return status;
  }
}

export function formatSessionDate(value: string): string {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "short",
    timeStyle: "short"
  }).format(new Date(value));
}

export function listMentorshipsAsMentor(status?: MentorshipStatus): Promise<MentorshipPage> {
  const query = status ? `?status=${status}` : "";
  return api<MentorshipPage>(`/api/v1/mentorships/as-mentor${query}`);
}

export function listMentorshipsAsMentee(status?: MentorshipStatus): Promise<MentorshipPage> {
  const query = status ? `?status=${status}` : "";
  return api<MentorshipPage>(`/api/v1/mentorships/as-mentee${query}`);
}

export function getMentorshipRelationship(id: string): Promise<MentorshipRelationship> {
  return api<MentorshipRelationship>(`/api/v1/mentorships/relationships/${id}`);
}

export function completeMentorship(id: string): Promise<MentorshipRelationship> {
  return api<MentorshipRelationship>(`/api/v1/mentorships/relationships/${id}/complete`, { method: "PATCH" });
}

export function cancelMentorship(id: string): Promise<MentorshipRelationship> {
  return api<MentorshipRelationship>(`/api/v1/mentorships/relationships/${id}/cancel`, { method: "PATCH" });
}

export function listMentorshipSessions(id: string): Promise<MentorshipSession[]> {
  return api<MentorshipSession[]>(`/api/v1/mentorships/relationships/${id}/sessions`);
}

export function createMentorshipSession(
  mentorshipId: string,
  input: { scheduledAt: string; durationMinutes: number; meetingUrl?: string; notes?: string }
): Promise<MentorshipSession> {
  return api<MentorshipSession>(`/api/v1/mentorships/relationships/${mentorshipId}/sessions`, {
    method: "POST",
    body: JSON.stringify({
      scheduledAt: input.scheduledAt,
      durationMinutes: input.durationMinutes,
      meetingUrl: input.meetingUrl?.trim() ? input.meetingUrl.trim() : null,
      notes: input.notes?.trim() ? input.notes.trim() : null
    })
  });
}

export function listAgenda(from?: string, to?: string): Promise<MentorshipSession[]> {
  const params = new URLSearchParams();
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  const query = params.toString();
  return api<MentorshipSession[]>(`/api/v1/agenda${query ? `?${query}` : ""}`);
}

export async function getNextAgendaSession(): Promise<MentorshipSession | null> {
  const session = await api<MentorshipSession | undefined>("/api/v1/agenda/next");
  return session ?? null;
}

export function completeSession(id: string, notes?: string): Promise<MentorshipSession> {
  return api<MentorshipSession>(`/api/v1/sessions/${id}/complete`, {
    method: "PATCH",
    body: JSON.stringify({ notes: notes?.trim() ? notes.trim() : null })
  });
}

export function cancelSession(id: string, reason?: string): Promise<MentorshipSession> {
  return api<MentorshipSession>(`/api/v1/sessions/${id}/cancel`, {
    method: "PATCH",
    body: JSON.stringify({ reason: reason?.trim() ? reason.trim() : null })
  });
}

export function markSessionNoShow(id: string): Promise<MentorshipSession> {
  return api<MentorshipSession>(`/api/v1/sessions/${id}/no-show`, { method: "PATCH" });
}
