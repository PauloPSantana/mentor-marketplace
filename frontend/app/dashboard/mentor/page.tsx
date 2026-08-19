"use client";

import Link from "next/link";
import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { EnrollmentList, sortEnrollments } from "@/components/enrollments/EnrollmentList";
import { MentorshipList } from "@/components/mentorships/MentorshipList";
import { EditAccountName } from "@/components/EditAccountName";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { api, apiErrorMessage } from "@/lib/api";
import { clearAuthSession, getStoredUser, roleLabel, type StoredUser } from "@/lib/auth";
import { listReceivedMentorshipRequests, type Enrollment } from "@/lib/enrollments";
import { listMentorshipsAsMentor, type MentorshipRelationship } from "@/lib/mentorships";
import { parseLinkedInUrl } from "@/lib/linkedin";

type MentorProfile = {
  id: string;
  headline: string | null;
  bio: string | null;
  yearsExperience: number | null;
  photoUrl: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  sessionPrice: number | null;
  modality: "ONLINE" | "PRESENCIAL" | "HIBRIDA" | null;
  skills: string[];
  technologies: string[];
  active: boolean;
  updatedAt?: string;
};

function joinTags(values: string[] | undefined): string {
  return (values ?? []).join(", ");
}

function splitTags(raw: FormDataEntryValue | null): string[] {
  if (typeof raw !== "string" || !raw.trim()) {
    return [];
  }
  return raw
    .split(",")
    .map((item) => item.trim())
    .filter(Boolean);
}

export default function MentorDashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<StoredUser | null>(null);
  const [profile, setProfile] = useState<MentorProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [ready, setReady] = useState(false);
  const [enrollments, setEnrollments] = useState<Enrollment[]>([]);
  const [enrollmentsError, setEnrollmentsError] = useState<string | null>(null);
  const [mentorships, setMentorships] = useState<MentorshipRelationship[]>([]);
  const [linkedinUrl, setLinkedinUrl] = useState("");

  useEffect(() => {
    const parsed = getStoredUser();
    if (!parsed) {
      router.replace("/login");
      return;
    }
    if (parsed.role !== "MENTOR") {
      router.replace("/dashboard/mentorado");
      return;
    }
    setUser(parsed);

    api<MentorProfile>("/api/v1/mentors/me")
      .then((loaded) => {
        setProfile(loaded);
        setLinkedinUrl(loaded.linkedinUrl ?? "");
      })
      .catch(() => setError("Não foi possível carregar o perfil."))
      .finally(() => setReady(true));

    listReceivedMentorshipRequests()
      .then((items) => setEnrollments(sortEnrollments(items)))
      .catch((err) => setEnrollmentsError(apiErrorMessage(err, "Não foi possível carregar as solicitações.")));

    listMentorshipsAsMentor()
      .then((page) => setMentorships(page.items))
      .catch(() => undefined);
  }, [router]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSuccess(null);
    setLoading(true);

    const form = new FormData(event.currentTarget);
    const sessionPriceRaw = String(form.get("sessionPrice") ?? "").trim();
    let normalizedLinkedIn: string | null = linkedinUrl.trim() || null;
    if (normalizedLinkedIn) {
      try {
        normalizedLinkedIn = parseLinkedInUrl(normalizedLinkedIn).url;
        setLinkedinUrl(normalizedLinkedIn);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Informe um link válido do LinkedIn.");
        setLoading(false);
        return;
      }
    }

    try {
      const updated = await api<MentorProfile>("/api/v1/mentors/me", {
        method: "PUT",
        body: JSON.stringify({
          headline: form.get("headline"),
          bio: form.get("bio"),
          yearsExperience: Number(form.get("yearsExperience") || 0),
          photoUrl: form.get("photoUrl") || null,
          linkedinUrl: normalizedLinkedIn,
          githubUrl: form.get("githubUrl") || null,
          sessionPrice: sessionPriceRaw ? Number(sessionPriceRaw) : null,
          modality: form.get("modality") || null,
          skills: splitTags(form.get("skills")),
          technologies: splitTags(form.get("technologies")),
          active: form.get("active") === "on"
        })
      });
      setProfile(updated);
      setLinkedinUrl(updated.linkedinUrl ?? "");
      setSuccess("Perfil atualizado com sucesso.");
    } catch {
      setError("Não foi possível salvar o perfil. Verifique os dados.");
    } finally {
      setLoading(false);
    }
  }

  if (!user || !ready) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  return (
    <main className="container" style={{ padding: "3rem 0", maxWidth: 720 }}>
      <h1>Dashboard do mentor</h1>
      <p style={{ marginBottom: "1.5rem", display: "flex", alignItems: "center", gap: "0.45rem", flexWrap: "wrap" }}>
        Olá, {user.name}
        <EditAccountName user={user} onUpdated={setUser} />
        <span className="role-badge">{roleLabel(user.role)}</span>
      </p>

      <section className="enrollment-section" id="meu-perfil">
        <h2 className="help-heading">
          Meu perfil
          <HelpTooltip
            text="Preencha título, bio e especialidades para aparecer melhor no catálogo. O nome é editado no lápis ao lado da saudação."
            href="/ajuda/perfil"
            label="Ajuda sobre o perfil"
          />
        </h2>
        <p style={{ color: "var(--muted)", marginBottom: "1rem" }}>
          Aqui você edita LinkedIn, bio, especialidades e as demais informações profissionais.
        </p>
      {!profile ? (
        <p className="error">{error ?? "Perfil indisponível."}</p>
      ) : (
      <form key={profile.updatedAt ?? profile.id} className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
        <label>
          Título profissional
          <input
            className="input"
            name="headline"
            maxLength={180}
            defaultValue={profile.headline ?? ""}
            placeholder="Ex.: Engenheiro de Software"
            required
          />
        </label>
        <label>
          Biografia
          <textarea
            className="input"
            name="bio"
            rows={4}
            defaultValue={profile.bio ?? ""}
            placeholder="Conte sua experiência e como você mentora"
          />
        </label>
        <label>
          Anos de experiência
          <input
            className="input"
            name="yearsExperience"
            type="number"
            min={0}
            defaultValue={profile.yearsExperience ?? 0}
          />
        </label>
        <label>
          Valor da sessão (R$)
          <input
            className="input"
            name="sessionPrice"
            type="number"
            min={0}
            step="0.01"
            defaultValue={profile.sessionPrice ?? ""}
          />
        </label>
        <label>
          Modalidade
          <select className="input" name="modality" defaultValue={profile.modality ?? "ONLINE"}>
            <option value="ONLINE">Online</option>
            <option value="PRESENCIAL">Presencial</option>
            <option value="HIBRIDA">Híbrida</option>
          </select>
        </label>
        <label>
          Especialidades (separadas por vírgula)
          <input
            className="input"
            name="skills"
            defaultValue={joinTags(profile.skills)}
            placeholder="Arquitetura de Software, Backend, Cloud"
          />
        </label>
        <label>
          Tecnologias (separadas por vírgula)
          <input
            className="input"
            name="technologies"
            defaultValue={joinTags(profile.technologies)}
            placeholder="Java, Spring Boot, Docker"
          />
        </label>
        <label>
          LinkedIn
          <input
            className="input"
            name="linkedinUrl"
            value={linkedinUrl}
            onChange={(event) => setLinkedinUrl(event.target.value)}
            placeholder="https://www.linkedin.com/in/seu-perfil"
          />
        </label>
        <label>
          GitHub
          <input className="input" name="githubUrl" defaultValue={profile.githubUrl ?? ""} />
        </label>
        <label>
          Foto (URL)
          <input className="input" name="photoUrl" defaultValue={profile.photoUrl ?? ""} />
        </label>
        <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
          <input name="active" type="checkbox" defaultChecked={profile.active ?? true} />
          Perfil ativo no marketplace
        </label>

        {error ? <p className="error">{error}</p> : null}
        {success ? <p style={{ color: "var(--accent)" }}>{success}</p> : null}

        <button className="btn" type="submit" disabled={loading}>
          {loading ? "Salvando..." : "Salvar perfil"}
        </button>
      </form>
      )}
      </section>

      <section className="enrollment-section">
        <h2>Solicitações de mentoria</h2>
        {enrollmentsError ? <p className="error">{enrollmentsError}</p> : null}
        <EnrollmentList items={enrollments} perspective="MENTOR" onChange={(items) => setEnrollments(sortEnrollments(items))} />
      </section>

      <section className="enrollment-section">
        <h2>Minhas mentorias</h2>
        <MentorshipList items={mentorships} perspective="MENTOR" />
      </section>

      <div style={{ display: "flex", gap: "0.75rem", marginTop: "1.5rem" }}>
        <Link href="/feed" className="btn">Ir para o feed</Link>
        <Link href="/agenda" className="btn secondary">Agenda</Link>
        <Link href="/mentorias" className="btn secondary">Ver catálogo</Link>
        <button
          className="btn secondary"
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
