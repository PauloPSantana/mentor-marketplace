"use client";

import { useEffect, useState } from "react";
import { listPostLikes, type PostLikeUser } from "@/lib/feed";
import { roleLabel } from "@/lib/auth";

type PostLikeListProps = {
  postId: string;
  likeCount: number;
  open: boolean;
  onToggle: () => void;
};

export function PostLikeList({ postId, likeCount, open, onToggle }: PostLikeListProps) {
  const [likers, setLikers] = useState<PostLikeUser[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }
    setLoading(true);
    setError(null);
    listPostLikes(postId)
      .then(setLikers)
      .catch(() => setError("Não foi possível carregar quem curtiu."))
      .finally(() => setLoading(false));
  }, [open, postId, likeCount]);

  if (likeCount === 0) {
    return <span className="post-like-summary">Nenhuma curtida ainda</span>;
  }

  return (
    <div className="post-like-summary">
      <button className="text-btn" type="button" onClick={onToggle}>
        {likeCount} {likeCount === 1 ? "curtida" : "curtidas"}
        {open ? " ▲" : " ▼"}
      </button>
      {open ? (
        <div className="post-likers">
          {loading ? <p className="post-meta">Carregando...</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {!loading && !error && likers.length === 0 ? (
            <p className="post-meta">Ninguém curtiu ainda.</p>
          ) : null}
          {!loading && !error
            ? likers.map((liker) => (
                <div key={`${liker.userId}-${liker.likedAt}`} className="post-liker">
                  <div className="avatar" aria-hidden="true">
                    {liker.name.trim().charAt(0).toUpperCase()}
                  </div>
                  <div>
                    <strong>{liker.name}</strong>
                    {liker.role ? <span className="role-badge">{roleLabel(liker.role)}</span> : null}
                  </div>
                </div>
              ))
            : null}
        </div>
      ) : null}
    </div>
  );
}
