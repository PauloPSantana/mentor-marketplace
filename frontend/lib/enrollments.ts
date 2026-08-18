import { api } from "@/lib/api";

export type EnrollmentStatus =
  | "PENDING"
  | "ACCEPTED"
  | "REJECTED"
  | "CANCELLED"
  | "EXPIRED"
  | "COMPLETED"
  | "ACTIVE";

export type MentorshipStatus = "ACTIVE" | "COMPLETED" | "CANCELLED";

export type Enrollment = {
  id: string;
  mentorshipId: string;
  menteeUserId: string;
  mentorProfileId: string;
  mentorUserId: string;
  menteeName: string;
  mentorName: string;
  productTitle: string;
  status: EnrollmentStatus;
  priceSnapshot: number;
  platformFee: number;
  mentorAmount: number;
  currency: string;
  message: string | null;
  createdAt: string;
  updatedAt: string;
  respondedAt: string | null;
  cancelledAt: string | null;
  activeMentorshipId: string | null;
  mentorshipStatus: MentorshipStatus | null;
};

export function enrollmentDisplayStatus(enrollment: Enrollment): EnrollmentStatus {
  if (enrollment.mentorshipStatus === "COMPLETED" || enrollment.status === "COMPLETED") {
    return "COMPLETED";
  }
  if (enrollment.status === "ACTIVE") {
    return "ACCEPTED";
  }
  return enrollment.status;
}

export function enrollmentStatusLabel(enrollment: Enrollment | EnrollmentStatus): string {
  const status = typeof enrollment === "string" ? enrollment : enrollmentDisplayStatus(enrollment);
  switch (status) {
    case "PENDING":
      return "Pendente";
    case "ACCEPTED":
    case "ACTIVE":
      return "Ativa";
    case "REJECTED":
      return "Recusada";
    case "CANCELLED":
      return "Cancelada";
    case "EXPIRED":
      return "Expirada";
    case "COMPLETED":
      return "Concluída";
    default:
      return status;
  }
}

export function isOpenEnrollment(enrollment: Enrollment): boolean {
  const status = enrollmentDisplayStatus(enrollment);
  return status === "PENDING" || status === "ACCEPTED";
}

export function canCompleteEnrollment(enrollment: Enrollment): boolean {
  return enrollment.mentorshipStatus === "ACTIVE";
}

export function formatMoney(value: number, currency = "BRL"): string {
  if (value == null || Number(value) <= 0) {
    return "A combinar";
  }
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency }).format(value);
}

export function requestMentorship(mentorId: string, message?: string): Promise<Enrollment> {
  return api<Enrollment>("/api/v1/mentorship-requests", {
    method: "POST",
    body: JSON.stringify({
      mentorId,
      message: message?.trim() ? message.trim() : null
    })
  });
}

export function listMyEnrollments(): Promise<Enrollment[]> {
  return api<Enrollment[]>("/api/v1/enrollments/me");
}

export function listSentMentorshipRequests(): Promise<Enrollment[]> {
  return api<Enrollment[]>("/api/v1/mentorship-requests/sent");
}

export function listReceivedMentorshipRequests(): Promise<Enrollment[]> {
  return api<Enrollment[]>("/api/v1/mentorship-requests/received");
}

export async function getMyEnrollmentForMentor(mentorId: string): Promise<Enrollment | null> {
  const enrollment = await api<Enrollment | undefined>(`/api/v1/mentors/${mentorId}/enrollments/me`);
  return enrollment ?? null;
}

export function acceptEnrollment(id: string): Promise<Enrollment> {
  return api<Enrollment>(`/api/v1/mentorship-requests/${id}/accept`, { method: "PATCH" });
}

export function rejectEnrollment(id: string): Promise<Enrollment> {
  return api<Enrollment>(`/api/v1/mentorship-requests/${id}/reject`, { method: "PATCH" });
}

export function cancelEnrollment(id: string): Promise<Enrollment> {
  return api<Enrollment>(`/api/v1/mentorship-requests/${id}/cancel`, { method: "PATCH" });
}

export function completeEnrollment(id: string): Promise<Enrollment> {
  return api<Enrollment>(`/api/v1/enrollments/${id}/complete`, { method: "PATCH" });
}
