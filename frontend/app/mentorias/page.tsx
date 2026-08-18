"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
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
      <h1>Catálogo de mentores</h1>
      <p className="feed-subtitle">Encontre mentores, siga perfis de interesse e acompanhe publicações no feed.</p>
      {error ? <p className="error">{error}</p> : null}
      {loading ? <p className="feed-status">Carregando mentores...</p> : null}
      {!loading ? (
        <section className="feed-list">
          {mentors.map((mentor) => {
            const initial = mentor.name.trim().charAt(0).toUpperCase();
            return (
              <article key={mentor.id} className="post-card">
                <header className="post-author">
                  <div className="avatar" aria-hidden="true">{initial}</div>
                  <div className="post-author-text">
                    <strong>{mentor.name}</strong>
                    <p className="post-meta">{mentor.headline ?? "Mentor"}</p>
                    {mentor.technologies.length > 0 ? (
                      <p className="post-meta">{mentor.technologies.slice(0, 4).join(" • ")}</p>
                    ) : null}
                  </div>
                  <Link className="btn secondary" href={`/mentors/${mentor.id}`}>
                    Ver perfil
                  </Link>
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
