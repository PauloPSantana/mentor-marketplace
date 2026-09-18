import { api } from "@/lib/api";

export type GroupStatus = "ACTIVE" | "CLOSED";
export type GroupMemberRole = "MENTOR" | "MENTEE";

export type GroupMember = {
  id: string;
  userId: string;
  name: string;
  role: GroupMemberRole;
  mentorshipId: string | null;
  joinedAt: string;
};

export type MentorshipGroup = {
  id: string;
  title: string;
  description: string | null;
  status: GroupStatus;
  ownerUserId: string;
  mentorUserId: string;
  mentorName: string;
  productId: string | null;
  productTitle: string | null;
  memberCount: number;
  owner: boolean;
  mentor: boolean;
  members: GroupMember[];
  createdAt: string;
};

export type GroupCandidate = {
  userId: string;
  name: string;
  role: GroupMemberRole;
  mentorshipId: string;
  serviceName: string;
};

export function listGroups(): Promise<MentorshipGroup[]> {
  return api<MentorshipGroup[]>("/api/v1/groups");
}

export function listGroupCandidates(): Promise<GroupCandidate[]> {
  return api<GroupCandidate[]>("/api/v1/groups/candidates");
}

export function createGroup(input: {
  title: string;
  description?: string;
  mentorshipIds: string[];
}): Promise<MentorshipGroup> {
  return api<MentorshipGroup>("/api/v1/groups", {
    method: "POST",
    body: JSON.stringify({
      title: input.title,
      description: input.description?.trim() ? input.description.trim() : null,
      mentorshipIds: input.mentorshipIds
    })
  });
}

export function getGroup(id: string): Promise<MentorshipGroup> {
  return api<MentorshipGroup>(`/api/v1/groups/${id}`);
}

export function listGroupAddCandidates(id: string): Promise<GroupCandidate[]> {
  return api<GroupCandidate[]>(`/api/v1/groups/${id}/candidates`);
}

export function addGroupMember(id: string, mentorshipId: string): Promise<MentorshipGroup> {
  return api<MentorshipGroup>(`/api/v1/groups/${id}/members`, {
    method: "POST",
    body: JSON.stringify({ mentorshipId })
  });
}

export function removeGroupMember(id: string, userId: string): Promise<MentorshipGroup> {
  return api<MentorshipGroup>(`/api/v1/groups/${id}/members/${userId}`, { method: "DELETE" });
}

export function closeGroup(id: string): Promise<MentorshipGroup> {
  return api<MentorshipGroup>(`/api/v1/groups/${id}/close`, { method: "PATCH" });
}

export function groupStatusLabel(status: GroupStatus): string {
  return status === "CLOSED" ? "Encerrado" : "Ativo";
}
