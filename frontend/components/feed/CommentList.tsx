"use client";

import { useState } from "react";
import { CommentForm } from "@/components/feed/CommentForm";
import { deleteComment, formatRelativeTime, createReply, type Comment } from "@/lib/feed";
import { roleLabel } from "@/lib/auth";
import { UserAvatar } from "@/components/UserAvatar";

type CommentListProps = {
  postId: string;
  comments: Comment[];
  currentUserId?: string;
  onChanged: () => void;
};

type CommentItemProps = {
  postId: string;
  comment: Comment;
  currentUserId?: string;
  depth?: number;
  onChanged: () => void;
};

function CommentItem({ postId, comment, currentUserId, depth = 0, onChanged }: CommentItemProps) {
  const [replying, setReplying] = useState(false);
  const owned = Boolean(currentUserId && currentUserId === comment.authorUserId);
  const deleted = comment.status === "DELETED";
  const canReply = depth === 0 && !deleted;

  async function onDelete() {
    if (!window.confirm("Excluir este comentário?")) {
      return;
    }
    try {
      await deleteComment(postId, comment.id);
      onChanged();
    } catch {
      window.alert("Não foi possível excluir o comentário.");
    }
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
                <button className="text-btn danger" type="button" onClick={onDelete}>
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
                await createReply(postId, comment.id, content);
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
            <CommentItem
              key={reply.id}
              postId={postId}
              comment={reply}
              currentUserId={currentUserId}
              depth={depth + 1}
              onChanged={onChanged}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export function CommentList({ postId, comments, currentUserId, onChanged }: CommentListProps) {
  if (comments.length === 0) {
    return <p className="post-meta">Nenhum comentário ainda. Seja o primeiro a comentar.</p>;
  }

  return (
    <div className="comment-list">
      {comments.map((comment) => (
        <CommentItem
          key={comment.id}
          postId={postId}
          comment={comment}
          currentUserId={currentUserId}
          onChanged={onChanged}
        />
      ))}
    </div>
  );
}
