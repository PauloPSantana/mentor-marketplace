"use client";

import { useEffect, useState } from "react";
import { EditAccountName } from "@/components/EditAccountName";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser, type StoredUser } from "@/lib/auth";
import { getInstitutionDashboard, getInstitutionMailStatus } from "@/lib/institutions";

export default function InstitutionSettingsPage() {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [institutionName, setInstitutionName] = useState("");
  const [mailEnabled, setMailEnabled] = useState<boolean | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setUser(getStoredUser());
    Promise.all([getInstitutionDashboard(), getInstitutionMailStatus()])
      .then(([dashboard, mail]) => {
        setInstitutionName(dashboard.name);
        setMailEnabled(mail.enabled);
      })
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível carregar as configurações.")));
  }, []);

  return (
    <main className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <h1>Configurações</h1>
          <p className="post-meta">Dados da instituição e envio de convites.</p>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      <section className="enrollment-section">
        <h2>Instituição</h2>
        <p className="post-meta">Nome: {institutionName || "—"}</p>
        {user ? (
          <p className="post-meta" style={{ display: "flex", alignItems: "center", gap: "0.45rem" }}>
            Responsável: {user.name}
            <EditAccountName user={user} onUpdated={setUser} />
          </p>
        ) : null}
      </section>
      <section className="enrollment-section">
        <h2>E-mail de convite</h2>
        <p className="post-meta">
          {mailEnabled == null
            ? "Consultando status..."
            : mailEnabled
              ? "O envio de convites por e-mail está ativo."
              : "O envio por e-mail está desativado. Os convites geram um link para você copiar e enviar."}
        </p>
      </section>
    </main>
  );
}
