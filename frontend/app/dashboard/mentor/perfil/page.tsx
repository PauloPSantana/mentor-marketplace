"use client";

import { useState } from "react";
import { AvailabilitySettingsCard } from "@/components/mentorships/AvailabilitySettingsCard";
import { GoogleConnectionCard } from "@/components/mentorships/GoogleConnectionCard";
import { ZoomConnectionCard } from "@/components/mentorships/ZoomConnectionCard";
import { MentorProfileForm } from "@/components/mentors/MentorProfileForm";
import { EditAccountName } from "@/components/EditAccountName";
import { getStoredUser, roleLabel, type StoredUser } from "@/lib/auth";

export default function MentorProfilePage() {
  const [user, setUser] = useState<StoredUser | null>(() => getStoredUser());
  const [mentorId, setMentorId] = useState<string | null>(null);

  if (!user) {
    return <main className="dashboard-page">Carregando...</main>;
  }

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Perfil</h1>
          <p className="post-meta" style={{ display: "flex", alignItems: "center", gap: "0.45rem", flexWrap: "wrap" }}>
            {user.name}
            <EditAccountName user={user} onUpdated={setUser} />
            <span className="role-badge">{roleLabel(user.role)}</span>
          </p>
        </div>
      </header>
      <MentorProfileForm onLoaded={(profile) => setMentorId(profile.id)} />
      <GoogleConnectionCard />
      {mentorId ? <AvailabilitySettingsCard mentorId={mentorId} /> : null}
      <ZoomConnectionCard />
    </main>
  );
}
