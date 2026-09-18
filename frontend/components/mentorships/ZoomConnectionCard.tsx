"use client";

import { apiErrorMessage } from "@/lib/api";
import { connectZoom, disconnectZoom, getZoomStatus, type ZoomStatus } from "@/lib/zoom";
import { useEffect, useState } from "react";

export function ZoomConnectionCard() {
  const [status, setStatus] = useState<ZoomStatus | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const zoom = params.get("zoom");
    if (zoom === "connected") {
      setMessage("Conta Zoom conectada.");
    } else if (zoom === "error") {
      setError("Não foi possível conectar o Zoom. Tente novamente.");
    }
    getZoomStatus()
      .then(setStatus)
      .catch((err) => setError(apiErrorMessage(err, "Não foi possível consultar o Zoom.")));
  }, []);

  if (!status || (!status.oauthEnabled && !status.accountMeetingsEnabled)) {
    return null;
  }

  async function onConnect() {
    setBusy(true);
    setError(null);
    try {
      await connectZoom();
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível iniciar a conexão com o Zoom."));
      setBusy(false);
    }
  }

  async function onDisconnect() {
    setBusy(true);
    setError(null);
    try {
      await disconnectZoom();
      setStatus(await getZoomStatus());
      setMessage("Conta Zoom desconectada.");
    } catch (err) {
      setError(apiErrorMessage(err, "Não foi possível desconectar o Zoom."));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="enrollment-section">
      <h2>Zoom</h2>
      <p className="post-meta">
        {status.connected
          ? "Sua conta Zoom está conectada. As sessões serão criadas nela."
          : status.accountMeetingsEnabled
            ? "As reuniões serão criadas pela plataforma. Conecte sua conta para usar seu próprio Zoom."
            : "Conecte sua conta Zoom para criar reuniões automaticamente."}
      </p>
      {error ? <p className="error">{error}</p> : null}
      {message ? <p style={{ color: "var(--accent)" }}>{message}</p> : null}
      {status.oauthEnabled ? (
        status.connected ? (
          <button className="btn secondary" type="button" disabled={busy} onClick={() => void onDisconnect()}>
            Desconectar Zoom
          </button>
        ) : (
          <button className="btn" type="button" disabled={busy} onClick={() => void onConnect()}>
            Conectar Zoom
          </button>
        )
      ) : null}
    </section>
  );
}
