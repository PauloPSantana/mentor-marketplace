"use client";

import { FormEvent, useState } from "react";

type CommentFormProps = {
  placeholder?: string;
  submitLabel?: string;
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
};

export function CommentForm({
  placeholder = "Escreva um comentário...",
  submitLabel = "Comentar",
  onSubmit,
  onCancel
}: CommentFormProps) {
  const [content, setContent] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!content.trim()) {
      return;
    }
    setError(null);
    setLoading(true);
    try {
      await onSubmit(content.trim());
      setContent("");
    } catch {
      setError("Não foi possível enviar o comentário.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <form className="comment-form" onSubmit={handleSubmit}>
      <textarea
        className="input post-textarea comment-textarea"
        value={content}
        onChange={(event) => setContent(event.target.value)}
        placeholder={placeholder}
        maxLength={2000}
        rows={2}
        required
      />
      {error ? <p className="error">{error}</p> : null}
      <div className="comment-form-actions">
        {onCancel ? (
          <button className="btn secondary" type="button" onClick={onCancel} disabled={loading}>
            Cancelar
          </button>
        ) : null}
        <button className="btn" type="submit" disabled={loading || !content.trim()}>
          {loading ? "Enviando..." : submitLabel}
        </button>
      </div>
    </form>
  );
}
