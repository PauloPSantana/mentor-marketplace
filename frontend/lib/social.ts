import { api } from "@/lib/api";

export type FollowStatus = {
  following: boolean;
  followerCount: number;
  followingCount: number;
  blocked?: boolean;
  blockedBy?: boolean;
};

export type FollowUser = {
  userId: string;
  name: string;
  role: string | null;
  followedAt: string;
  mentorProfileId?: string | null;
};

export type FollowList = {
  items: FollowUser[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export type BlockStatus = {
  blocked: boolean;
  blockedBy: boolean;
};

export const FOLLOW_EVENT = "mentorhub:follow";

const followStatusCache = new Map<string, FollowStatus>();

export function getCachedFollowStatus(userId: string): FollowStatus | undefined {
  return followStatusCache.get(userId);
}

export function setCachedFollowStatus(userId: string, status: FollowStatus): void {
  followStatusCache.set(userId, status);
  if (typeof window !== "undefined") {
    window.dispatchEvent(new CustomEvent(FOLLOW_EVENT, { detail: { userId, status } }));
  }
}

export function clearCachedFollowStatus(userId: string): void {
  followStatusCache.delete(userId);
}

export function followUser(userId: string): Promise<FollowStatus> {
  return api<FollowStatus>(`/api/v1/users/${userId}/follow`, { method: "POST" }).then((status) => {
    setCachedFollowStatus(userId, status);
    return status;
  });
}

export function unfollowUser(userId: string): Promise<FollowStatus> {
  return api<FollowStatus>(`/api/v1/users/${userId}/follow`, { method: "DELETE" }).then((status) => {
    setCachedFollowStatus(userId, status);
    return status;
  });
}

export function getFollowStatus(userId: string): Promise<FollowStatus> {
  const cached = followStatusCache.get(userId);
  if (cached) {
    return Promise.resolve(cached);
  }
  return api<FollowStatus>(`/api/v1/users/${userId}/follow-status`).then((status) => {
    setCachedFollowStatus(userId, status);
    return status;
  });
}

export function listFollowers(userId: string, page = 0, size = 20): Promise<FollowList> {
  return api<FollowList>(`/api/v1/users/${userId}/followers?page=${page}&size=${size}`);
}

export function listFollowing(userId: string, page = 0, size = 20): Promise<FollowList> {
  return api<FollowList>(`/api/v1/users/${userId}/following?page=${page}&size=${size}`);
}

export function blockUser(userId: string): Promise<BlockStatus> {
  return api<BlockStatus>(`/api/v1/users/${userId}/block`, { method: "POST" });
}

export function unblockUser(userId: string): Promise<BlockStatus> {
  return api<BlockStatus>(`/api/v1/users/${userId}/block`, { method: "DELETE" });
}

export function getBlockStatus(userId: string): Promise<BlockStatus> {
  return api<BlockStatus>(`/api/v1/users/${userId}/block-status`);
}
