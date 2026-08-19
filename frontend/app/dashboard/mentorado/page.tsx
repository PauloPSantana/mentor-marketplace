"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { EnrollmentList, sortEnrollments } from "@/components/enrollments/EnrollmentList";
import { MentorshipList } from "@/components/mentorships/MentorshipList";
import { EditAccountName } from "@/components/EditAccountName";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { apiErrorMessage } from "@/lib/api";
import { clearAuthSession, getStoredUser, roleLabel, type StoredUser } from "@/lib/auth";
import { listSentMentorshipRequests, type Enrollment } from "@/lib/enrollments";
import { listMentorshipsAsMentee, type MentorshipRelationship } from "@/lib/mentorships";

export default function MentoradoDashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const parsed = getStoredUser();
    if (!parsed) {
      router.replace("/login");
      return;
    }
    if (parsed.role !== "MENTEE") {
      router.replace("/dashboard/mentor");
      return;
    }
    setUser(parsed);
    Promise.all([listSentMentorshipRequests(), listMentorshipsAsMentee()])
      .then(([items, page]) => {
        setEnrollments(sortEnrollments(items));
        setMentorships(page.items);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar suas solicitações.")))
      .finally(() => setLoading(false));
  }, [router]);

  if (!user) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  return (
    <main className="container" style={{ padding: "3rem 0" }}>
      <h1>Dashboard do mentorado</h1>
      <p style={{ display: "flex", alignItems: "center", gap: "0.45rem", flexWrap: "wrap" }}>
        Olá, {user.name}
        <EditAccountName user={user} onUpdated={setUser} />
        <span className="role-badge">{roleLabel(user.role)}</span>
      </p>
      <section className="enrollment-section">
        <h2 className="help-heading">
          Minhas solicitações
          <HelpTooltip
            text="Acompanhe pedidos pendentes, aceitos ou recusados. Você pode cancelar enquanto estiver pendente."
            href="/ajuda/mentorias"
            label="Ajuda sobre solicitações"
          />
        </h2>
        {error ? <p className="error">{error}</p> : null}
        {loading ? <p className="feed-status">Carregando solicitações...</p> : (
          <EnrollmentList items={enrollments} perspective="MENTEE" onChange={(items) => setEnrollments(sortEnrollments(items))} />
        )}
      </section>
      <section className="enrollment-section">
        <h2>Minhas mentorias</h2>
        <MentorshipList items={mentorships} perspective="MENTEE" />
      </section>
      <div style={{ display: "flex", gap: "0.75rem", marginTop: "1.5rem" }}>
        <Link href="/feed" className="btn">Ir para o feed</Link>
        <Link href="/agenda" className="btn secondary">Agenda</Link>
        <Link href="/mentorias" className="btn secondary">Buscar mentorias</Link>
        <button
          className="btn"
          type="button"
          onClick={() => {
            clearAuthSession();
            router.push("/");
          }}
        >
          Sair
        </button>
      </div>
    </main>
  );
}
