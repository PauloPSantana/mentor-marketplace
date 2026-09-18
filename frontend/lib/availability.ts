import { api } from "@/lib/api";

export type AvailabilityRule = {
  dayOfWeek: "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";
  startTime: string;
  endTime: string;
  active: boolean;
};

export type AvailabilitySlot = {
  startAt: string;
  label: string;
};

export type AvailabilityDay = {
  date: string;
  slots: AvailabilitySlot[];
};

export type MentorAvailability = {
  slotDurationMinutes: number;
  bufferMinutes: number;
  timezone: string;
  rules: AvailabilityRule[];
  days: AvailabilityDay[];
};

export function getMentorAvailability(mentorId: string, from?: string, to?: string): Promise<MentorAvailability> {
  const params = new URLSearchParams();
  if (from) params.set("from", from);
  if (to) params.set("to", to);
  const query = params.toString();
  return api<MentorAvailability>(`/api/v1/mentors/${mentorId}/availability${query ? `?${query}` : ""}`);
}

export function saveMentorAvailability(
  mentorId: string,
  input: {
    slotDurationMinutes: number;
    bufferMinutes: number;
    timezone?: string;
    rules: AvailabilityRule[];
  }
): Promise<MentorAvailability> {
  return api<MentorAvailability>(`/api/v1/mentors/${mentorId}/availability`, {
    method: "PUT",
    body: JSON.stringify({
      slotDurationMinutes: input.slotDurationMinutes,
      bufferMinutes: input.bufferMinutes,
      timezone: input.timezone ?? "America/Sao_Paulo",
      rules: input.rules
    })
  });
}

export function bookMentorSlot(
  mentorId: string,
  input: { mentorshipId: string; startAt: string }
): Promise<import("@/lib/mentorships").MentorshipSession> {
  return api(`/api/v1/mentors/${mentorId}/bookings`, {
    method: "POST",
    body: JSON.stringify(input)
  });
}
