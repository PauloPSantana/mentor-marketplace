"use client";

import { FormEvent, useCallback, useEffect, useRef, useState } from "react";
import { CommentButton } from "@/components/feed/CommentButton";
import { CommentForm } from "@/components/feed/CommentForm";
import { CommentList } from "@/components/feed/CommentList";
import { LikeButton } from "@/components/feed/LikeButton";
import { PostLikeList } from "@/components/feed/PostLikeList";
import { ShareButton } from "@/components/feed/ShareButton";
import { FollowButton } from "@/components/social/FollowButton";
import {
  createComment,
  deletePost,
  formatRelativeTime,
  likePost,
  listComments,
  unlikePost,
  updatePost,
  type Comment,
  type Post
} from "@/lib/feed";

type PostCardProps = {
  post: Post;
  currentUserId?: string;
  onUpdated: (post: Post) => void;
  onDeleted: (postId: string) => void;
};

export function PostCard({ post, currentUserId, onUpdated, onDeleted }: PostCardProps) {
  const owned = Boolean(currentUserId && currentUserId === post.authorUserId);
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(post.content);
  const [imageUrl, setImageUrl] = useState(post.imageUrl ?? "");
  const [error, setError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const [liking, setLiking] = useState(false);
  const [showLikers, setShowLikers] = useState(false);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const postRef = useRef(post);
  const onUpdatedRef = useRef(onUpdated);
  postRef.current = post;
  onUpdatedRef.current = onUpdated;

  const refreshComments = useCallback(async () => {
    setCommentsLoading(true);
    try {
      const response = await listComments(postRef.current.id);
      setComments(response.items);
      onUpdatedRef.current({ ...postRef.current, commentCount: response.totalCount });
    } catch {
      setError("Não foi possível carregar os comentários.");
    } finally {
      setCommentsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (!showComments) {
      return;
    }
    void refreshComments();
  }, [showComments, post.id, refreshComments]);

  async function onSave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaving(true);
    try {
      const updated = await updatePost(post.id, content, imageUrl);
      onUpdated(updated);
      setEditing(false);
    } catch {
      setError("Não foi possível salvar a publicação.");
    } finally {
      setSaving(false);
    }
  }

  async function onDelete() {
    if (!window.confirm("Excluir esta publicação?")) {
      return;
    }
    setError(null);
    try {
      await deletePost(post.id);
      onDeleted(post.id);
    } catch {
      setError("Não foi possível excluir a publicação.");
    }
  }

  async function onToggleLike() {
    setError(null);
    setLiking(true);
    try {
      const result = post.liked ? await unlikePost(post.id) : await likePost(post.id);
      onUpdated({ ...post, liked: result.liked, likeCount: result.likeCount });
    } catch {
      setError("Não foi possível atualizar a curtida.");
    } finally {
      setLiking(false);
    }
  }

  async function onCreateComment(text: string) {
    await createComment(post.id, text);
    await refreshComments();
  }

  async function onCommentsChanged() {
    await refreshComments();
  }

  function toggleComments() {
    setShowComments((value) => !value);
  }

  const initial = post.authorName.trim().charAt(0).toUpperCase();
  const commentLabel =
    post.commentCount === 1 ? "1 comentário" : `${post.commentCount ?? 0} comentários`;

  return (
    <article className="post-card" id={`post-${post.id}`}>
      <header className="post-author">
        {post.authorPhotoUrl ? (
          // eslint-disable-next-line @next/next/no-img-element
          <img className="avatar" src={post.authorPhotoUrl} alt="" />
        ) : (
          <div className="avatar" aria-hidden="true">
            {initial}
          </div>
        )}
        <div className="post-author-text">
          <strong>{post.authorName}</strong>
          <p className="post-meta">
            {post.authorHeadline || (post.authorRole === "MENTOR" ? "Mentor" : "Mentorado")}
            {" • "}
            {formatRelativeTime(post.createdAt)}
          </p>
        </div>
        {owned ? (
          <div className="post-owner-actions">
            <button className="text-btn" type="button" onClick={() => setEditing((value) => !value)}>
              {editing ? "Cancelar" : "Editar"}
            </button>
            <button className="text-btn danger" type="button" onClick={onDelete}>
              Excluir
            </button>
          </div>
        ) : (
          <FollowButton userId={post.authorUserId} compact />
        )}
      </header>

      {editing ? (
        <form className="post-edit" onSubmit={onSave}>
          <textarea
            className="input post-textarea"
            value={content}
            onChange={(event) => setContent(event.target.value)}
            maxLength={5000}
            required
            rows={4}
          />
          <input
            className="input"
            value={imageUrl}
            onChange={(event) => setImageUrl(event.target.value)}
            placeholder="URL da imagem (opcional)"
            maxLength={500}
          />
          <button className="btn" type="submit" disabled={saving || !content.trim()}>
            {saving ? "Salvando..." : "Salvar"}
          </button>
        </form>
      ) : (
        <>
          <p className="post-content">{post.content}</p>
          {post.imageUrl ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img className="post-image" src={post.imageUrl} alt="" />
          ) : null}
        </>
      )}

      {error ? <p className="error">{error}</p> : null}

      <div className="post-stats">
        {owned ? (
          <PostLikeList
            postId={post.id}
            likeCount={post.likeCount ?? 0}
            open={showLikers}
            onToggle={() => setShowLikers((value) => !value)}
          />
        ) : (
          <span>👍 {post.likeCount ?? 0}</span>
        )}
        <button className="text-btn" type="button" onClick={toggleComments}>
          {commentLabel}
        </button>
      </div>
      <div className="post-actions">
        <LikeButton liked={Boolean(post.liked)} pending={liking} onToggle={onToggleLike} />
        <CommentButton active={showComments} onClick={toggleComments} />
        <ShareButton />
      </div>

      {showComments ? (
        <section className="post-comments">
          <CommentForm onSubmit={onCreateComment} />
          {commentsLoading ? <p className="post-meta">Carregando comentários...</p> : null}
          {!commentsLoading ? (
            <CommentList
              postId={post.id}
              comments={comments}
              currentUserId={currentUserId}
              onChanged={onCommentsChanged}
            />
          ) : null}
        </section>
      ) : null}
    </article>
  );
}
