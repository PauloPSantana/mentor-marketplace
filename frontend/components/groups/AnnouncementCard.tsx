"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { CommentButton } from "@/components/feed/CommentButton";
import { CommentForm } from "@/components/feed/CommentForm";
import { LikeButton } from "@/components/feed/LikeButton";
import { UserAvatar } from "@/components/UserAvatar";
import { apiErrorMessage } from "@/lib/api";
import { roleLabel } from "@/lib/auth";
import { formatRelativeTime } from "@/lib/feed";
import {
  createAnnouncementComment,
  deleteAnnouncementComment,
  likeAnnouncement,
  listAnnouncementComments,
  replyAnnouncementComment,
  unlikeAnnouncement,
  audienceLabel,
  type Announcement,
  type AnnouncementComment
} from "@/lib/announcements";

type AnnouncementCardProps = {
  groupId: string;
  announcement: Announcement;
  currentUserId?: string;
  canInteract: boolean;
  onUpdated: (announcement: Announcement) => void;
};

export function AnnouncementCard({
  groupId,
  announcement,
  currentUserId,
  canInteract,
  onUpdated
}: AnnouncementCardProps) {
  const [error, setError] = useState<string | null>(null);
  const [liking, setLiking] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<AnnouncementComment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const announcementRef = useRef(announcement);
  const onUpdatedRef = useRef(onUpdated);
  announcementRef.current = announcement;
  onUpdatedRef.current = onUpdated;

  const refreshComments = useCallback(async () => {
    setCommentsLoading(true);
    try {
      const response = await listAnnouncementComments(groupId, announcementRef.current.id);
      setComments(response.items);
      onUpdatedRef.current({ ...announcementRef.current, commentCount: response.totalCount });
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível carregar os comentários."));
    } finally {
      setCommentsLoading(false);
    }
  }, [groupId]);

  useEffect(() => {
    if (!showComments) {
      return;
    }
    void refreshComments();
  }, [showComments, announcement.id, refreshComments]);

  async function onToggleLike() {
    if (!canInteract) {
      return;
    }
    setError(null);
    setLiking(true);
    try {
      const result = announcement.liked
        ? await unlikeAnnouncement(groupId, announcement.id)
        : await likeAnnouncement(groupId, announcement.id);
      onUpdated({ ...announcement, liked: result.liked, likeCount: result.likeCount });
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar a curtida."));
    } finally {
      setLiking(false);
    }
  }

  return (
    <article className="enrollment-item" style={{ display: "grid", gap: "0.75rem" }}>
      <div className="comment-header">
        <UserAvatar name={announcement.authorName} photoUrl={announcement.authorPhotoUrl} />
        <div>
          <p className="enrollment-title">{announcement.authorName}</p>
          <p className="post-meta">
            {roleLabel(announcement.authorRole)} · {formatRelativeTime(announcement.createdAt)} ·{" "}
            {audienceLabel(announcement.audienceType, announcement.recipients)}
          </p>
        </div>
      </div>
      {announcement.title ? <h3 style={{ margin: 0 }}>{announcement.title}</h3> : null}
      <p style={{ margin: 0, whiteSpace: "pre-wrap" }}>{announcement.body}</p>
      {error ? <p className="error">{error}</p> : null}
      <div className="enrollment-actions">
        <LikeButton liked={announcement.liked} pending={liking || !canInteract} onToggle={() => void onToggleLike()} />
        <span className="post-meta">{announcement.likeCount} curtida(s)</span>
        <CommentButton active={showComments} onClick={() => setShowComments((value) => !value)} />
        <span className="post-meta">
          {announcement.commentCount === 1 ? "1 comentário" : `${announcement.commentCount} comentários`}
        </span>
      </div>
      {showComments ? (
        <div>
          {commentsLoading ? <p className="post-meta">Carregando comentários...</p> : null}
          <AnnouncementCommentList
            groupId={groupId}
            announcementId={announcement.id}
            comments={comments}
            currentUserId={currentUserId}
            canInteract={canInteract}
            onChanged={() => void refreshComments()}
          />
          {canInteract ? (
            <CommentForm
              onSubmit={async (content) => {
                await createAnnouncementComment(groupId, announcement.id, content);
                await refreshComments();
              }}
            />
          ) : null}
        </div>
      ) : null}
    </article>
  );
}

function AnnouncementCommentList({
  groupId,
  announcementId,
  comments,
  currentUserId,
  canInteract,
  onChanged
}: {
  groupId: string;
  announcementId: string;
  comments: AnnouncementComment[];
  currentUserId?: string;
  canInteract: boolean;
  onChanged: () => void;
}) {
  if (comments.length === 0) {
    return <p className="post-meta">Nenhum comentário ainda. Seja o primeiro a comentar.</p>;
  }
  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <AnnouncementCommentItem
          key={comment.id}
          groupId={groupId}
          announcementId={announcementId}
          comment={comment}
          currentUserId={currentUserId}
          canInteract={canInteract}
          onChanged={onChanged}
        />
      ))}
    </div>
  );
}

function AnnouncementCommentItem({
  groupId,
  announcementId,
  comment,
  currentUserId,
  canInteract,
  depth = 0,
  onChanged
}: {
  groupId: string;
  announcementId: string;
  comment: AnnouncementComment;
  currentUserId?: string;
  canInteract: boolean;
  depth?: number;
  onChanged: () => void;
}) {
  const [replying, setReplying] = useState(false);
  const owned = Boolean(currentUserId && currentUserId === comment.authorUserId);
  const deleted = comment.status === "DELETED";
  const canReply = depth === 0 && !deleted && canInteract;

  async function onDelete() {
    if (!window.confirm("Excluir este comentário?")) {
      return;
    }
    await deleteAnnouncementComment(groupId, announcementId, comment.id);
    onChanged();
  }

  return (
    <div className={`comment-item${depth > 0 ? " comment-reply" : ""}${deleted ? " comment-deleted" : ""}`}>
      <div className="comment-header">
        <UserAvatar name={comment.authorName} photoUrl={comment.authorPhotoUrl} />
        <div className="comment-body">
          <div className="comment-meta">
            <strong>{comment.authorName}</strong>
            <span className="role-badge">{roleLabel(comment.authorRole)}</span>
            <span className="post-meta">• {formatRelativeTime(comment.createdAt)}</span>
          </div>
          <p className="comment-content">{comment.content}</p>
          {!deleted ? (
            <div className="comment-actions">
              {canReply ? (
                <button className="text-btn" type="button" onClick={() => setReplying((value) => !value)}>
                  {replying ? "Cancelar" : "Responder"}
                </button>
              ) : null}
              {owned ? (
                <button className="text-btn danger" type="button" onClick={() => void onDelete()}>
                  Excluir
                </button>
              ) : null}
            </div>
          ) : null}
          {replying ? (
            <CommentForm
              placeholder="Escreva uma resposta..."
              submitLabel="Responder"
              onCancel={() => setReplying(false)}
              onSubmit={async (content) => {
                await replyAnnouncementComment(groupId, announcementId, comment.id, content);
                setReplying(false);
                onChanged();
              }}
            />
          ) : null}
        </div>
      </div>
      {comment.replies.length > 0 ? (
        <div className="comment-replies">
          {comment.replies.map((reply) => (
            <AnnouncementCommentItem
              key={reply.id}
              groupId={groupId}
              announcementId={announcementId}
              comment={reply}
              currentUserId={currentUserId}
              canInteract={canInteract}
              depth={depth + 1}
              onChanged={onChanged}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}
