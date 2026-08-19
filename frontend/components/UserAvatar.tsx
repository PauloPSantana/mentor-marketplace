"use client";

import { useRef, useState } from "react";
import { apiErrorMessage, apiUpload } from "@/lib/api";
import { getStoredUser, updateStoredUser, type StoredUser } from "@/lib/auth";
import { mediaUrl } from "@/lib/media";

type UserResponse = {
  id: string;
  name: string;
  email: string;
  role: string;
  photoUrl?: string | null;
  updatedAt?: string | null;
};

type UserAvatarProps = {
  name?: string | null;
  photoUrl?: string | null;
  photoVersion?: string | number | null;
  editable?: boolean;
  onUploaded?: (user: StoredUser) => void;
};

export function UserAvatar({ name, photoUrl, photoVersion, editable = false, onUploaded }: UserAvatarProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const src = mediaUrl(photoUrl, photoVersion);
  const initial = name?.trim()?.charAt(0)?.toUpperCase() ?? "?";

  async function onFileChange(file: File | undefined) {
    if (!file || !editable) {
      return;
    }
    setError(null);
    setLoading(true);
    try {
      const updated = await apiUpload<UserResponse>("/api/v1/auth/me/photo", file);
      const current = getStoredUser();
      const nextUser: StoredUser = {
        ...(current ?? { name: updated.name, email: updated.email, role: updated.role }),
        id: updated.id,
        name: updated.name,
        email: updated.email,
        role: updated.role,
        photoUrl: updated.photoUrl,
        updatedAt: updated.updatedAt
      };
      updateStoredUser(nextUser);
      onUploaded?.(nextUser);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível enviar a foto."));
    } finally {
      setLoading(false);
      if (inputRef.current) {
        inputRef.current.value = "";
      }
    }
  }

  const avatar = src ? (
    // eslint-disable-next-line @next/next/no-img-element
    <img className="avatar" src={src} alt="" />
  ) : (
    <div className="avatar" aria-hidden="true">
      {initial}
    </div>
  );

  if (!editable) {
    return avatar;
  }

  return (
    <div className="avatar-editable">
      <button
        type="button"
        className="avatar-button"
        aria-label="Alterar foto de perfil"
        title="Alterar foto"
        disabled={loading}
        onClick={() => inputRef.current?.click()}
      >
        {avatar}
        <span className="avatar-edit-hint">{loading ? "..." : "Foto"}</span>
      </button>
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        hidden
        onChange={(event) => onFileChange(event.target.files?.[0])}
      />
      {error ? <p className="error avatar-error">{error}</p> : null}
    </div>
  );
}
