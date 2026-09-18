import { api } from "@/lib/api";

export type AnnouncementAudienceType = "GROUP" | "MEMBERS";
export type AnnouncementCommentStatus = "ACTIVE" | "DELETED";

export type AnnouncementRecipient = {
  userId: string;
  name: string;
};

export type Announcement = {
  id: string;
  groupId: string;
  title: string | null;
  body: string;
  audienceType: AnnouncementAudienceType;
  authorUserId: string;
  authorName: string;
  authorPhotoUrl: string | null;
  authorRole: string;
  recipients: AnnouncementRecipient[];
  likeCount: number;
  liked: boolean;
  commentCount: number;
  createdAt: string;
};

export type AnnouncementComment = {
  id: string;
  announcementId: string;
  parentCommentId: string | null;
  authorUserId: string;
  content: string;
  authorName: string;
  authorPhotoUrl: string | null;
  authorRole: string;
  status: AnnouncementCommentStatus;
  createdAt: string;
  replies: AnnouncementComment[];
};

export type AnnouncementComments = {
  totalCount: number;
  items: AnnouncementComment[];
};

export type AnnouncementLike = {
  announcementId: string;
  liked: boolean;
  likeCount: number;
};

export function listAnnouncements(groupId: string): Promise<Announcement[]> {
  return api<Announcement[]>(`/api/v1/groups/${groupId}/announcements`);
}

export function createAnnouncement(
  groupId: string,
  input: {
    title?: string;
    body: string;
    audienceType: AnnouncementAudienceType;
    recipientUserIds?: string[];
  }
): Promise<Announcement> {
  return api<Announcement>(`/api/v1/groups/${groupId}/announcements`, {
    method: "POST",
    body: JSON.stringify({
      title: input.title?.trim() ? input.title.trim() : null,
      body: input.body,
      audienceType: input.audienceType,
      recipientUserIds: input.recipientUserIds ?? []
    })
  });
}

export function likeAnnouncement(groupId: string, announcementId: string): Promise<AnnouncementLike> {
  return api<AnnouncementLike>(`/api/v1/groups/${groupId}/announcements/${announcementId}/likes`, {
    method: "POST"
  });
}

export function unlikeAnnouncement(groupId: string, announcementId: string): Promise<AnnouncementLike> {
  return api<AnnouncementLike>(`/api/v1/groups/${groupId}/announcements/${announcementId}/likes`, {
    method: "DELETE"
  });
}

export function listAnnouncementComments(groupId: string, announcementId: string): Promise<AnnouncementComments> {
  return api<AnnouncementComments>(`/api/v1/groups/${groupId}/announcements/${announcementId}/comments`);
}

export function createAnnouncementComment(
  groupId: string,
  announcementId: string,
  content: string
): Promise<AnnouncementComment> {
  return api<AnnouncementComment>(`/api/v1/groups/${groupId}/announcements/${announcementId}/comments`, {
    method: "POST",
    body: JSON.stringify({ content })
  });
}

export function replyAnnouncementComment(
  groupId: string,
  announcementId: string,
  commentId: string,
  content: string
): Promise<AnnouncementComment> {
  return api<AnnouncementComment>(
    `/api/v1/groups/${groupId}/announcements/${announcementId}/comments/${commentId}/replies`,
    {
      method: "POST",
      body: JSON.stringify({ content })
    }
  );
}

export function deleteAnnouncementComment(
  groupId: string,
  announcementId: string,
  commentId: string
): Promise<void> {
  return api<void>(`/api/v1/groups/${groupId}/announcements/${announcementId}/comments/${commentId}`, {
    method: "DELETE"
  });
}

export function audienceLabel(type: AnnouncementAudienceType, recipients: AnnouncementRecipient[]): string {
  if (type === "GROUP") {
    return "Todo o grupo";
  }
  if (recipients.length === 1) {
    return recipients[0].name;
  }
  return `${recipients.length} mentorados`;
}
