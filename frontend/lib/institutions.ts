import { api } from "@/lib/api";

export type InstitutionMentor = {
  mentorProfileId: string;
  userId: string;
  name: string;
  specialty: string | null;
  menteeCount: number;
  sessionCount: number;
  active: boolean;
};

export type InstitutionInvitation = {
  id: string;
  name: string;
  email: string;
  specialty: string | null;
  program: string | null;
  status: "PENDING" | "ACCEPTED" | "CANCELLED" | "EXPIRED";
  inviteUrl: string;
  expiresAt: string;
  emailSent?: boolean;
};

export type InstitutionDashboard = {
  id: string;
  name: string;
  mentors: number;
  mentees: number;
  activeMentorships: number;
  completedSessions: number;
  mentorList: InstitutionMentor[];
  invitations: InstitutionInvitation[];
};

export type PublicMentorInvitation = {
  name: string;
  email: string;
  specialty: string | null;
  program: string | null;
  institutionName: string;
  status: string;
  expiresAt: string;
};

export function getInstitutionDashboard(): Promise<InstitutionDashboard> {
  return api("/api/v1/institutions/me");
}

export function getInstitutionMailStatus(): Promise<{ enabled: boolean }> {
  return api("/api/v1/institutions/me/mail-status");
}

export function inviteMentor(input: {
  name: string;
  email: string;
  specialty?: string;
  program?: string;
}): Promise<InstitutionInvitation> {
  return api("/api/v1/institutions/me/invitations", {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function deactivateInstitutionMentor(mentorProfileId: string): Promise<void> {
  return api(`/api/v1/institutions/me/mentors/${mentorProfileId}/deactivate`, {
    method: "POST"
  });
}

export function getPublicInvitation(token: string): Promise<PublicMentorInvitation> {
  return api(`/api/v1/mentor-invitations/${token}`);
}

export function acceptMentorInvitation(
  token: string,
  input: { password: string; confirmPassword: string }
): Promise<{ accessToken: string; user: { id: string; name: string; email: string; role: string } }> {
  return api(`/api/v1/mentor-invitations/${token}/accept`, {
    method: "POST",
    body: JSON.stringify(input)
  });
}

export function resendMentorInvitation(invitationId: string): Promise<InstitutionInvitation> {
  return api(`/api/v1/institutions/me/invitations/${invitationId}/resend`, {
    method: "POST"
  });
}

export function removeMentorInvitation(invitationId: string): Promise<void> {
  return api(`/api/v1/institutions/me/invitations/${invitationId}`, {
    method: "DELETE"
  });
}

export function cancelMentorInvitation(invitationId: string): Promise<void> {
  return api(`/api/v1/institutions/me/invitations/${invitationId}/cancel`, {
    method: "POST"
  });
}

export function invitationStatusLabel(status: InstitutionInvitation["status"]): string {
  if (status === "PENDING") return "Pendente";
  if (status === "ACCEPTED") return "Aceito";
  if (status === "EXPIRED") return "Expirado";
  if (status === "CANCELLED") return "Cancelado";
  return status;
}

export function invitationStatusClass(status: InstitutionInvitation["status"]): string {
  if (status === "PENDING") return "pending";
  if (status === "ACCEPTED") return "accepted";
  if (status === "EXPIRED") return "expired";
  return "cancelled";
}
