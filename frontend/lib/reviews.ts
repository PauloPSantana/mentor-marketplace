import { api } from "@/lib/api";

export type ReviewStatus = "ACTIVE" | "HIDDEN" | "REMOVED";

export type Review = {
  id: string;
  mentorshipId: string;
  reviewerId: string;
  reviewerName: string;
  reviewedUserId: string;
  rating: number;
  comment: string | null;
  status: ReviewStatus;
  createdAt: string;
  updatedAt: string;
};

export type MentorRating = {
  ratingAvg: number;
  ratingCount: number;
};

export function listMentorshipReviews(mentorshipId: string): Promise<Review[]> {
  return api<Review[]>(`/api/v1/mentorships/relationships/${mentorshipId}/reviews`);
}

export function createMentorshipReview(
  mentorshipId: string,
  input: { rating: number; comment?: string }
): Promise<Review> {
  return api<Review>(`/api/v1/mentorships/relationships/${mentorshipId}/reviews`, {
    method: "POST",
    body: JSON.stringify({
      rating: input.rating,
      comment: input.comment?.trim() ? input.comment.trim() : null
    })
  });
}

export function listMentorReviews(mentorProfileId: string): Promise<Review[]> {
  return api<Review[]>(`/api/v1/mentors/${mentorProfileId}/reviews`);
}

export function getMentorRating(mentorProfileId: string): Promise<MentorRating> {
  return api<MentorRating>(`/api/v1/mentors/${mentorProfileId}/rating`);
}
