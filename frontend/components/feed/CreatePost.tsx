"use client";

import { FormEvent, useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { createPost, type Post } from "@/lib/feed";
import { getStoredUser, type StoredUser } from "@/lib/auth";

type CreatePostProps = {
  onCreated: (post: Post) => void;
};

export function CreatePost({ onCreated }: CreatePostProps) {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [content, setContent] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setUser(getStoredUser());
  }, []);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const post = await createPost(content, imageUrl);
      setContent("");
      setImageUrl("");
      onCreated(post);
    } catch (error) {
      setError(apiErrorMessage(error, "Não foi possível publicar. Tente novamente."));
    } finally {
      setLoading(false);
    }
  }

  const initial = user?.name?.trim()?.charAt(0)?.toUpperCase() ?? "?";

  return (
    <form className="post-card create-post" onSubmit={onSubmit}>
      <div className="post-author">
        <div className="avatar" aria-hidden="true">
          {initial}
        </div>
        <div>
          <strong>{user?.name ?? "Você"}</strong>
          <p className="post-meta">Compartilhe uma ideia com a comunidade</p>
        </div>
      </div>
      <textarea
        className="input post-textarea"
        value={content}
        onChange={(event) => setContent(event.target.value)}
        placeholder="No que você está pensando?"
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
      {error ? <p className="error">{error}</p> : null}
      <div className="create-post-actions">
        <button className="btn" type="submit" disabled={loading || !content.trim()}>
          {loading ? "Publicando..." : "Publicar"}
        </button>
      </div>
    </form>
  );
}
