"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { BlockButton } from "@/components/social/BlockButton";
import { FollowButton } from "@/components/social/FollowButton";
import { FollowListPanel } from "@/components/social/FollowListPanel";
import { RequestMentorshipCard } from "@/components/enrollments/RequestMentorshipCard";
import { UserAvatar } from "@/components/UserAvatar";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { getMentor, type MentorProfile } from "@/lib/mentors";
import { listMentorReviews, type Review } from "@/lib/reviews";
import type { FollowStatus } from "@/lib/social";

export default function MentorProfilePage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const mentorId = params.id;
  const [mentor, setMentor] = useState<MentorProfile | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [followStatus, setFollowStatus] = useState<FollowStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const currentUser = getStoredUser();
  const isSelf = Boolean(currentUser?.id && mentor?.userId === currentUser.id);

  useEffect(() => {
    if (!getStoredUser()) {
      router.replace("/login");
      return;
    }
    setLoading(true);
    Promise.all([getMentor(mentorId), listMentorReviews(mentorId)])
      .then(([profile, items]) => {
        setMentor(profile);
        setReviews(items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar o mentor.")))
      .finally(() => setLoading(false));
  }, [mentorId, router]);

  if (loading) {
    return <main className="container feed-page">Carregando perfil...</main>;
  }

  if (error || !mentor) {
    return (
      <main className="container feed-page">
        <p className="error">{error ?? "Mentor não encontrado."}</p>
        <Link className="text-btn" href="/mentorias">Voltar ao catálogo</Link>
      </main>
    );
  }

  return (
    <main className="container feed-page">
      <Link className="text-btn" href="/mentorias">← Voltar ao catálogo</Link>
      <article className="post-card mentor-profile-card">
        <header className="post-author">
          <UserAvatar name={mentor.name} photoUrl={mentor.photoUrl} />
          <div className="post-author-text">
            <h1 style={{ margin: 0 }}>{mentor.name}</h1>
            <p className="post-meta">{mentor.headline ?? "Mentor"}</p>
            <p className="post-meta">
              {mentor.ratingCount > 0
                ? `${Number(mentor.ratingAvg ?? 0).toFixed(1)} • ${mentor.ratingCount} avaliação(ões)`
                : "Ainda sem avaliações"}
            </p>
            <p className="post-meta">
              {followStatus?.followerCount ?? 0} seguidores • {followStatus?.followingCount ?? 0} seguindo
            </p>
          </div>
          {!isSelf ? (
            <div className="profile-actions">
              <FollowButton userId={mentor.userId} onChange={setFollowStatus} />
              <BlockButton
                userId={mentor.userId}
                blocked={Boolean(followStatus?.blocked)}
                onChange={setFollowStatus}
              />
            </div>
          ) : null}
        </header>

        {mentor.bio ? <p className="post-content">{mentor.bio}</p> : null}

        {mentor.technologies.length > 0 ? (
          <p className="post-meta">
            <strong>Tecnologias:</strong> {mentor.technologies.join(", ")}
          </p>
        ) : null}

        {mentor.skills.length > 0 ? (
          <p className="post-meta">
            <strong>Especialidades:</strong> {mentor.skills.join(", ")}
          </p>
        ) : null}

        {mentor.sessionPrice != null ? (
          <p className="post-meta">
            <strong>Sessão:</strong> R$ {Number(mentor.sessionPrice).toFixed(2)}
          </p>
        ) : null}
      </article>
      {!isSelf ? (
        <RequestMentorshipCard
          mentorId={mentor.id}
          sessionPrice={mentor.sessionPrice}
          blocked={Boolean(followStatus?.blocked || followStatus?.blockedBy)}
        />
      ) : null}
      <section className="enrollment-section">
        <h2>Avaliações</h2>
        {reviews.length === 0 ? (
          <p className="feed-empty">Este mentor ainda não recebeu avaliações públicas.</p>
        ) : (
          <ul className="enrollment-list">
            {reviews.map((review) => (
              <li key={review.id} className="enrollment-item">
                <div>
                  <p className="enrollment-title">
                    {review.reviewerName} • {review.rating}/5
                  </p>
                  {review.comment ? <p className="post-content">{review.comment}</p> : null}
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
      <FollowListPanel
        key={`${followStatus?.followerCount ?? 0}-${followStatus?.followingCount ?? 0}`}
        userId={mentor.userId}
      />
    </main>
  );
}
