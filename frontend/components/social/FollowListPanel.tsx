"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiErrorMessage } from "@/lib/api";
import { roleLabel } from "@/lib/auth";
import { listFollowers, listFollowing, type FollowUser } from "@/lib/social";

type FollowListPanelProps = {
  userId: string;
};

type ListKind = "followers" | "following";

export function FollowListPanel({ userId }: FollowListPanelProps) {
  const [kind, setKind] = useState<ListKind>("followers");
  const [items, setItems] = useState<FollowUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    const request = kind === "followers" ? listFollowers(userId) : listFollowing(userId);
    request
      .then((response) => {
        if (!cancelled) {
          setItems(response.items);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(apiErrorMessage(err, "Não foi possível carregar a lista."));
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
  }, [userId, kind]);

  return (
    <section className="follow-list-panel">
      <div className="feed-tabs" role="tablist" aria-label="Listas de follow">
        <button
          type="button"
          role="tab"
          aria-selected={kind === "followers"}
          className={kind === "followers" ? "feed-tab feed-tab-active" : "feed-tab"}
          onClick={() => setKind("followers")}
        >
          Seguidores
        </button>
        <button
          type="button"
          role="tab"
          aria-selected={kind === "following"}
          className={kind === "following" ? "feed-tab feed-tab-active" : "feed-tab"}
          onClick={() => setKind("following")}
        >
          Seguindo
        </button>
      </div>
      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando...</p> : null}
      {!loading && items.length === 0 ? (
        <p className="feed-empty">
          {kind === "followers" ? "Nenhum seguidor ainda." : "Ainda não segue ninguém."}
        </p>
      ) : null}
      {!loading ? (
        <ul className="follow-list">
          {items.map((item) => {
            const href = item.mentorProfileId ? `/mentors/${item.mentorProfileId}` : null;
            const content = (
              <>
                <div className="avatar" aria-hidden="true">
                  {item.name.trim().charAt(0).toUpperCase()}
                </div>
                <div className="post-author-text">
                  <strong>{item.name}</strong>
                  <p className="post-meta">{roleLabel(item.role ?? "")}</p>
                </div>
              </>
            );
            return (
              <li key={item.userId} className="follow-list-item">
                {href ? (
                  <Link className="follow-list-link" href={href}>
                    {content}
                  </Link>
                ) : (
                  <div className="follow-list-link">{content}</div>
                )}
              </li>
            );
          })}
        </ul>
      ) : null}
    </section>
  );
}
