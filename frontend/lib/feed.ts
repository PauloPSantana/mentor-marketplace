import { api } from "@/lib/api";

export type Post = {
  id: string;
  authorUserId: string;
  content: string;
  imageUrl: string | null;
  authorName: string;
  authorPhotoUrl: string | null;
  authorHeadline: string | null;
  authorRole: string;
  createdAt: string;
  updatedAt: string;
  likeCount: number;
  liked: boolean;
  commentCount: number;
  followingAuthor?: boolean;
  score?: number | null;
};

export type FeedType = "FOR_YOU" | "FOLLOWING" | "RECENT";

export type Comment = {
  id: string;
  postId: string;
  parentCommentId: string | null;
  authorUserId: string;
  content: string;
  authorName: string;
  authorPhotoUrl: string | null;
  authorHeadline: string | null;
  authorRole: string;
  createdAt: string;
  replies: Comment[];
};

export type PostComments = {
  totalCount: number;
  items: Comment[];
};

export type PostLike = {
  postId: string;
  liked: boolean;
  likeCount: number;
};

export type PostLikeUser = {
  userId: string;
  name: string;
  role: "MENTOR" | "MENTEE" | "ADMIN" | string | null;
  likedAt: string;
};

export type PostFeed = {
  items: Post[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  type?: FeedType;
};

export function listPosts(page = 0, size = 10, type: FeedType = "RECENT"): Promise<PostFeed> {
  return api<PostFeed>(`/api/v1/feed?page=${page}&size=${size}&type=${type}`).then((feed) => ({
    ...feed,
    items: (feed.items ?? []).map(normalizePost)
  }));
}

export function createPost(content: string, imageUrl?: string): Promise<Post> {
  return api<Post>("/api/v1/posts", {
    method: "POST",
    body: JSON.stringify({
      content,
      imageUrl: imageUrl?.trim() ? imageUrl.trim() : null
    })
  }).then(normalizePost);
}

export function updatePost(id: string, content: string, imageUrl?: string): Promise<Post> {
  return api<Post>(`/api/v1/posts/${id}`, {
    method: "PUT",
    body: JSON.stringify({
      content,
      imageUrl: imageUrl?.trim() ? imageUrl.trim() : null
    })
  }).then(normalizePost);
}

export function deletePost(id: string): Promise<void> {
  return api<void>(`/api/v1/posts/${id}`, { method: "DELETE" });
}

export function likePost(id: string): Promise<PostLike> {
  return api<PostLike>(`/api/v1/posts/${id}/likes`, { method: "POST" });
}

export function unlikePost(id: string): Promise<PostLike> {
  return api<PostLike>(`/api/v1/posts/${id}/likes`, { method: "DELETE" });
}

export function getPostLike(id: string): Promise<PostLike> {
  return api<PostLike>(`/api/v1/posts/${id}/likes`);
}

export function listPostLikes(id: string): Promise<PostLikeUser[]> {
  return api<PostLikeUser[]>(`/api/v1/posts/${id}/likes/users`);
}

export function listComments(postId: string): Promise<PostComments> {
  return api<PostComments>(`/api/v1/posts/${postId}/comments`);
}

export function createComment(postId: string, content: string, parentCommentId?: string): Promise<Comment> {
  return api<Comment>(`/api/v1/posts/${postId}/comments`, {
    method: "POST",
    body: JSON.stringify({
      content,
      parentCommentId: parentCommentId ?? null
    })
  }).then(normalizeComment);
}

export function deleteComment(postId: string, commentId: string): Promise<void> {
  return api<void>(`/api/v1/posts/${postId}/comments/${commentId}`, { method: "DELETE" });
}

export function normalizePost(post: Post): Post {
  return {
    ...post,
    imageUrl: post.imageUrl ?? null,
    authorPhotoUrl: post.authorPhotoUrl ?? null,
    authorHeadline: post.authorHeadline ?? null,
    likeCount: post.likeCount ?? 0,
    liked: Boolean(post.liked),
    commentCount: post.commentCount ?? 0,
    followingAuthor: Boolean(post.followingAuthor),
    score: post.score ?? null
  };
}

export function normalizeComment(comment: Comment): Comment {
  return {
    ...comment,
    parentCommentId: comment.parentCommentId ?? null,
    authorPhotoUrl: comment.authorPhotoUrl ?? null,
    authorHeadline: comment.authorHeadline ?? null,
    replies: (comment.replies ?? []).map(normalizeComment)
  };
}

export function formatRelativeTime(iso: string): string {
  const date = new Date(iso);
  const diffMs = Date.now() - date.getTime();
  const minutes = Math.max(0, Math.floor(diffMs / 60_000));
  if (minutes < 1) {
    return "agora";
  }
  if (minutes < 60) {
    return `${minutes}min`;
  }
  const hours = Math.floor(minutes / 60);
  if (hours < 24) {
    return `${hours}h`;
  }
  const days = Math.floor(hours / 24);
  if (days < 7) {
    return `${days}d`;
  }
  return date.toLocaleDateString("pt-BR");
}
