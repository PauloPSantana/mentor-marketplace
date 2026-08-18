"use client";

import { useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { blockUser, clearCachedFollowStatus, getFollowStatus, setCachedFollowStatus, unblockUser, type FollowStatus } from "@/lib/social";

type BlockButtonProps = {
  userId: string;
  blocked: boolean;
  onChange?: (status: FollowStatus) => void;
};

export function BlockButton({ userId, blocked, onChange }: BlockButtonProps) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onToggle() {
    if (!blocked && !window.confirm("Bloquear este usuário? Vocês deixarão de se seguir.")) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      if (blocked) {
        await unblockUser(userId);
      } else {
        await blockUser(userId);
      }
      clearCachedFollowStatus(userId);
      const followStatus = await getFollowStatus(userId);
      setCachedFollowStatus(userId, followStatus);
      onChange?.(followStatus);
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível atualizar o bloqueio."));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="follow-button-wrap">
      <button className="text-btn danger" type="button" disabled={loading} onClick={() => void onToggle()}>
        {loading ? "..." : blocked ? "Desbloquear" : "Bloquear"}
      </button>
      {error ? <p className="error">{error}</p> : null}
    </div>
  );
}
