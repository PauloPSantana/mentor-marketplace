import { api } from "@/lib/api";

export type MentorProfile = {
  id: string;
  userId: string;
  name: string;
  headline: string | null;
  bio: string | null;
  yearsExperience: number | null;
  photoUrl: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  sessionPrice: number | null;
  modality: string | null;
  skills: string[];
  technologies: string[];
  verified: boolean;
  ratingAvg: number | null;
  ratingCount: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export function listMentors(): Promise<MentorProfile[]> {
  return api<MentorProfile[]>("/api/v1/mentors");
}

export function getMentor(id: string): Promise<MentorProfile> {
  return api<MentorProfile>(`/api/v1/mentors/${id}`);
}
