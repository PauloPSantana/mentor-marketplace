"use client";

import { FormEvent, useEffect, useRef, useState } from "react";
import { UserAvatar } from "@/components/UserAvatar";
import { api, apiErrorMessage, apiUpload } from "@/lib/api";
import { AUTH_EVENT, getStoredUser, updateStoredUser, type StoredUser } from "@/lib/auth";
import { createPost, type Post } from "@/lib/feed";

type UserResponse = {
  id: string;
  name: string;
  email: string;
  role: string;
  photoUrl?: string | null;
  updatedAt?: string | null;
};

type MentorProfile = {
  photoUrl: string | null;
};

type CreatePostProps = {
  onCreated: (post: Post) => void;
};

export function CreatePost({ onCreated }: CreatePostProps) {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [content, setContent] = useState("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    function sync() {
      setUser(getStoredUser());
    }
    sync();
    window.addEventListener(AUTH_EVENT, sync);
    return () => window.removeEventListener(AUTH_EVENT, sync);
  }, []);

  useEffect(() => {
    const stored = getStoredUser();
    if (!stored) {
      return;
    }
    let cancelled = false;
    async function loadPhoto() {
      try {
        const me = await api<UserResponse>("/api/v1/auth/me");
        let photoUrl = me.photoUrl ?? null;
        if (!photoUrl && me.role === "MENTOR") {
          const profile = await api<MentorProfile>("/api/v1/mentors/me");
          photoUrl = profile.photoUrl;
        }
        if (cancelled) {
          return;
        }
        const latest = getStoredUser() ?? stored;
        const nextUser: StoredUser = {
          ...latest,
          id: me.id,
          name: me.name,
          email: me.email,
          role: me.role,
          photoUrl,
          updatedAt: me.updatedAt
        };
        updateStoredUser(nextUser);
        setUser(nextUser);
      } catch {
        // Keep the locally stored user if the refresh fails.
      }
    }
    void loadPhoto();
    return () => {
      cancelled = true;
    };
  }, []);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      let uploadedUrl: string | undefined;
      if (imageFile) {
        const res = await apiUpload<{ url: string }>("/api/v1/posts/image", imageFile);
        uploadedUrl = res.url;
      }
      const post = await createPost(content, uploadedUrl);
      setContent("");
      setImageFile(null);
      setImagePreview(null);
      if (fileInputRef.current) fileInputRef.current.value = "";
      onCreated(post);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível publicar. Tente novamente."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="post-card create-post" onSubmit={onSubmit}>
      <div className="post-author">
        <UserAvatar
          name={user?.name}
          photoUrl={user?.photoUrl}
          photoVersion={user?.updatedAt}
          editable
          onUploaded={setUser}
        />
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
      <div className="create-post-image-upload">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          style={{ display: "none" }}
          onChange={(event) => {
            const file = event.target.files?.[0] ?? null;
            setImageFile(file);
            if (file) {
              setImagePreview(URL.createObjectURL(file));
            } else {
              setImagePreview(null);
            }
          }}
        />
        <button
          type="button"
          className="btn secondary"
          onClick={() => fileInputRef.current?.click()}
        >
          {imageFile ? "Trocar imagem" : "Adicionar imagem"}
        </button>
        {imagePreview && (
          <div className="create-post-image-preview">
            <img src={imagePreview} alt="Prévia" />
            <button
              type="button"
              className="btn-remove-image"
              onClick={() => {
                setImageFile(null);
                setImagePreview(null);
                if (fileInputRef.current) fileInputRef.current.value = "";
              }}
            >
              ✕
            </button>
          </div>
        )}
      </div>
      {error ? <p className="error">{error}</p> : null}
      <div className="create-post-actions">
        <button className="btn" type="submit" disabled={loading || !content.trim()}>
          {loading ? "Publicando..." : "Publicar"}
        </button>
      </div>
    </form>
  );
}
