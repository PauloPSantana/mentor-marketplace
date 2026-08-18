"use client";

import { useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import {
  FOLLOW_EVENT,
  followUser,
  getCachedFollowStatus,
  getFollowStatus,
  unfollowUser,
  type FollowStatus
} from "@/lib/social";

type FollowButtonProps = {
  userId: string;
  compact?: boolean;
  initialStatus?: FollowStatus;
  onChange?: (status: FollowStatus) => void;
};

export function FollowButton({ userId, compact = false, initialStatus, onChange }: FollowButtonProps) {
  const [status, setStatus] = useState<FollowStatus | null>(
    initialStatus ?? getCachedFollowStatus(userId) ?? null
  );
  const [loading, setLoading] = useState(!status);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const cached = getCachedFollowStatus(userId);
    if (cached) {
      setStatus(cached);
      setLoading(false);
      onChange?.(cached);
      return;
    }
    if (initialStatus) {
      setStatus(initialStatus);
      setLoading(false);
      return;
    }

    let cancelled = false;
    setLoading(true);
    getFollowStatus(userId)
      .then((response) => {
        if (!cancelled) {
          setStatus(response);
          onChange?.(response);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, "Não foi possível carregar o status de follow."));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [userId, initialStatus, onChange]);

  useEffect(() => {
    function sync(event: Event) {
      const detail = (event as CustomEvent<{ userId: string; status: FollowStatus }>).detail;
      if (detail?.userId === userId) {
        setStatus(detail.status);
      }
    }
    window.addEventListener(FOLLOW_EVENT, sync);
    return () => window.removeEventListener(FOLLOW_EVENT, sync);
  }, [userId]);

  async function onToggle() {
    if (!status) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response = status.following ? await unfollowUser(userId) : await followUser(userId);
      setStatus(response);
      onChange?.(response);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar o follow."));
    } finally {
      setLoading(false);
    }
  }

  if (loading && !status) {
    return compact ? null : <span className="post-meta">Carregando...</span>;
  }

  if (!status) {
    return error && !compact ? <span className="error">{error}</span> : null;
  }

  return (
    <div className="follow-button-wrap">
      <button
        className={compact ? `text-btn follow-compact${status.following ? " following" : ""}` : "btn secondary"}
        type="button"
        disabled={loading}
        onClick={() => void onToggle()}
      >
        {loading ? "..." : status.following ? (compact ? "Seguindo" : "Deixar de seguir") : compact ? "+ Seguir" : "Seguir"}
      </button>
      {error && !compact ? <p className="error">{error}</p> : null}
    </div>
  );
}
