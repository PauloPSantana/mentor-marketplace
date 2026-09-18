"use client";

import { apiErrorMessage } from "@/lib/api";
import { connectGoogle, disconnectGoogle, getGoogleStatus, type GoogleStatus } from "@/lib/google";
import { useEffect, useState } from "react";

export function GoogleConnectionCard() {
  const [status, setStatus] = useState<GoogleStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const google = params.get("google");
    if (google === "connected") {
      setMessage("Conta Google conectada.");
    } else if (google === "error") {
      setError("Não foi possível conectar o Google. Tente novamente.");
    }
    getGoogleStatus()
      .then(setStatus)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível consultar o Google.")));
  }, []);

  if (!status || !status.oauthEnabled) {
    return null;
  }

  async function onConnect() {
    setBusy(true);
    setError(null);
    try {
      await connectGoogle();
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível iniciar a conexão com o Google."));
      setBusy(false);
    }
  }

  async function onDisconnect() {
    setBusy(true);
    setError(null);
    try {
      await disconnectGoogle();
      setStatus(await getGoogleStatus());
      setMessage("Conta Google desconectada.");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível desconectar o Google."));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="enrollment-section">
      <h2>Google Calendar + Meet</h2>
      <p className="post-meta">
        {status.connected
          ? `Conta ${status.email ?? "Google"} conectada. Se agendar sessão falhar, desconecte e conecte de novo para conceder Calendar e horários ocupados.`
          : "Conecte sua conta Google para criar eventos no Calendar e gerar o link do Meet."}
      </p>
      {error ? <p className="error">{error}</p> : null}
      {message ? <p style={{ color: "var(--accent)" }}>{message}</p> : null}
      {status.connected ? (
        <button className="btn secondary" type="button" disabled={busy} onClick={() => void onDisconnect()}>
          Desconectar Google
        </button>
      ) : (
        <button className="btn" type="button" disabled={busy} onClick={() => void onConnect()}>
          Conectar Google
        </button>
      )}
    </section>
  );
}
