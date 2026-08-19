"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { FollowButton } from "@/components/social/FollowButton";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { UserAvatar } from "@/components/UserAvatar";
import { listMentors, type MentorProfile } from "@/lib/mentors";

export default function MentoriasPage() {
  const router = useRouter();
  const [mentors, setMentors] = useState<MentorProfile[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!getStoredUser()) {
      router.replace("/login");
      return;
    }
    listMentors()
      .then(setMentors)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar os mentores.")))
      .finally(() => setLoading(false));
  }, [router]);

  return (
    <main className="container feed-page">
      <h1 className="help-heading">
        Catálogo de mentores
        <HelpTooltip
          text="Abra um perfil para seguir, ver avaliações e solicitar mentoria."
          href="/ajuda/mentores"
          label="Ajuda sobre o catálogo"
        />
      </h1>
      <p className="feed-subtitle">Encontre mentores, siga perfis de interesse e acompanhe publicações no feed.</p>
      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando mentores...</p> : null}
      {!loading ? (
        <section className="feed-list">
          {mentors.map((mentor) => {
            return (
              <article key={mentor.id} className="post-card">
                <header className="post-author">
                  <UserAvatar name={mentor.name} photoUrl={mentor.photoUrl} />
                  <div className="post-author-text">
                    <strong>{mentor.name}</strong>
                    <p className="post-meta">{mentor.headline ?? "Mentor"}</p>
                    {mentor.technologies.length > 0 ? (
                      <p className="post-meta">{mentor.technologies.slice(0, 4).join(" • ")}</p>
                    ) : null}
                  </div>
                  <div className="profile-actions">
                    <Link className="btn secondary" href={`/mentors/${mentor.id}`}>
                      Ver perfil
                    </Link>
                    {mentor.userId !== getStoredUser()?.id ? (
                      <FollowButton userId={mentor.userId} compact />
                    ) : null}
                  </div>
                </header>
              </article>
            );
          })}
          {mentors.length === 0 ? <p className="feed-empty">Nenhum mentor ativo no momento.</p> : null}
        </section>
      ) : null}
    </main>
  );
}
