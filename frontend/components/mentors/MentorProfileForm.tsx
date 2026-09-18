"use client";

import { FormEvent, useEffect, useState } from "react";
import { HelpTooltip } from "@/components/help/HelpTooltip";
import { api, apiErrorMessage } from "@/lib/api";
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
  if (typeof raw !== "string" || !raw.trim()) return [];
  return raw.split(",").map((item) => item.trim()).filter(Boolean);
}

type MentorProfileFormProps = {
  onLoaded?: (profile: MentorProfile) => void;
};

export function MentorProfileForm({ onLoaded }: MentorProfileFormProps) {
  const [profile, setProfile] = useState<MentorProfile | null>(null);
  const [linkedinUrl, setLinkedinUrl] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api<MentorProfile>("/api/v1/mentors/me")
      .then((loaded) => {
        setProfile(loaded);
        setLinkedinUrl(loaded.linkedinUrl ?? "");
        onLoaded?.(loaded);
      })
      .catch(() => setError("Não foi possível carregar o perfil."));
  }, [onLoaded]);

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
      onLoaded?.(updated);
      setSuccess("Perfil atualizado com sucesso.");
    } catch {
      setError("Não foi possível salvar o perfil. Verifique os dados.");
    } finally {
      setLoading(false);
    }
  }

  if (!profile) {
    return <p className={error ? "error" : "feed-status"}>{error ?? "Carregando perfil..."}</p>;
  }

  return (
    <section className="enrollment-section">
      <h2 className="help-heading">
        Meu perfil
        <HelpTooltip
          text="Preencha título, bio e especialidades para aparecer melhor no catálogo."
          href="/ajuda/perfil"
          label="Ajuda sobre o perfil"
        />
      </h2>
      <form key={profile.updatedAt ?? profile.id} className="card-form" onSubmit={onSubmit} style={{ display: "grid", gap: "1rem" }}>
        <label>Título profissional<input className="input" name="headline" maxLength={180} defaultValue={profile.headline ?? ""} required /></label>
        <label>Biografia<textarea className="input" name="bio" rows={4} defaultValue={profile.bio ?? ""} /></label>
        <label>Anos de experiência<input className="input" name="yearsExperience" type="number" min={0} defaultValue={profile.yearsExperience ?? 0} /></label>
        <label>Valor da sessão (R$)<input className="input" name="sessionPrice" type="number" min={0} step="0.01" defaultValue={profile.sessionPrice ?? ""} /></label>
        <label>
          Modalidade
          <select className="input" name="modality" defaultValue={profile.modality ?? "ONLINE"}>
            <option value="ONLINE">Online</option>
            <option value="PRESENCIAL">Presencial</option>
            <option value="HIBRIDA">Híbrida</option>
          </select>
        </label>
        <label>Especialidades (separadas por vírgula)<input className="input" name="skills" defaultValue={joinTags(profile.skills)} /></label>
        <label>Tecnologias (separadas por vírgula)<input className="input" name="technologies" defaultValue={joinTags(profile.technologies)} /></label>
        <label>LinkedIn<input className="input" name="linkedinUrl" value={linkedinUrl} onChange={(event) => setLinkedinUrl(event.target.value)} /></label>
        <label>GitHub<input className="input" name="githubUrl" defaultValue={profile.githubUrl ?? ""} /></label>
        <label>Foto (URL)<input className="input" name="photoUrl" defaultValue={profile.photoUrl ?? ""} /></label>
        <label style={{ display: "flex", alignItems: "center", gap: "0.5rem" }}>
          <input name="active" type="checkbox" defaultChecked={profile.active ?? true} />
          Perfil ativo no marketplace
        </label>
        {error ? <p className="error">{error}</p> : null}
        {success ? <p style={{ color: "var(--accent)" }}>{success}</p> : null}
        <button className="btn" type="submit" disabled={loading}>{loading ? "Salvando..." : "Salvar perfil"}</button>
      </form>
    </section>
  );
}
