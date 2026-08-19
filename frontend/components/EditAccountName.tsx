"use client";

import { FormEvent, useEffect, useId, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { api, apiErrorMessage } from "@/lib/api";
import { updateStoredUser, type StoredUser } from "@/lib/auth";

type UserResponse = {
  id: string;
  name: string;
  email: string;
  role: string;
  photoUrl?: string | null;
  updatedAt?: string | null;
};

type EditAccountNameProps = {
  user: StoredUser;
  onUpdated: (user: StoredUser) => void;
};

function PencilIcon() {
  return (
    <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M14.1 4.4 19.6 9.9"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
      />
      <path
        d="M3.8 16.7 16.6 3.9a2.15 2.15 0 0 1 3 0l.5.5a2.15 2.15 0 0 1 0 3L7.3 20.2l-3.8 1.1 1.3-4.6Z"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinejoin="round"
      />
      <path
        d="M12.8 6.1 17.9 11.2"
        stroke="currentColor"
        strokeWidth="1.7"
        strokeLinecap="round"
      />
    </svg>
  );
}

export function EditAccountName({ user, onUpdated }: EditAccountNameProps) {
  const titleId = useId();
  const inputRef = useRef<HTMLInputElement>(null);
  const [open, setOpen] = useState(false);
  const [name, setName] = useState(user.name);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!open) {
      return;
    }
    setName(user.name);
    setError(null);
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    const timer = window.setTimeout(() => inputRef.current?.focus(), 0);
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape" && !loading) {
        setOpen(false);
      }
    }
    window.addEventListener("keydown", onKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      window.clearTimeout(timer);
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [open, user.name, loading]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const updated = await api<UserResponse>("/api/v1/auth/me", {
        method: "PUT",
        body: JSON.stringify({ name: name.trim() })
      });
      const nextUser: StoredUser = {
        ...user,
        id: updated.id,
        name: updated.name,
        email: updated.email,
        role: updated.role,
        photoUrl: updated.photoUrl ?? user.photoUrl,
        updatedAt: updated.updatedAt ?? user.updatedAt
      };
      updateStoredUser(nextUser);
      onUpdated(nextUser);
      setOpen(false);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar o nome."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <button
        type="button"
        className="icon-edit-btn"
        aria-label="Editar nome"
        title="Editar nome"
        onClick={() => setOpen(true)}
      >
        <PencilIcon />
      </button>
      {open && typeof document !== "undefined"
        ? createPortal(
            <div
              className="modal-backdrop"
              onClick={() => {
                if (!loading) {
                  setOpen(false);
                }
              }}
            >
              <div
                className="modal-card"
                role="dialog"
                aria-modal="true"
                aria-labelledby={titleId}
                onClick={(event) => event.stopPropagation()}
              >
                <h2 id={titleId}>Editar nome</h2>
                <form onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
                  <label>
                    Nome
                    <input
                      ref={inputRef}
                      className="input"
                      value={name}
                      onChange={(event) => setName(event.target.value)}
                      required
                      maxLength={120}
                      placeholder="Seu nome"
                    />
                  </label>
                  {error ? <p className="error">{error}</p> : null}
                  <div className="modal-actions">
                    <button
                      className="btn secondary"
                      type="button"
                      disabled={loading}
                      onClick={() => setOpen(false)}
                    >
                      Cancelar
                    </button>
                    <button
                      className="btn"
                      type="submit"
                      disabled={loading || name.trim() === "" || name.trim() === user.name.trim()}
                    >
                      {loading ? "Salvando..." : "Salvar"}
                    </button>
                  </div>
                </form>
              </div>
            </div>,
            document.body
          )
        : null}
    </>
  );
}
