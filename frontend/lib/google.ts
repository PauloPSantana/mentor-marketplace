import { api } from "@/lib/api";

export type GoogleStatus = {
  oauthEnabled: boolean;
  connected: boolean;
  email: string | null;
};

export function getGoogleStatus(): Promise<GoogleStatus> {
  return api<GoogleStatus>("/api/integrations/google/status");
}

export async function connectGoogle(): Promise<void> {
  const response = await api<{ authorizationUrl: string }>("/api/integrations/google/connect");
  window.location.href = response.authorizationUrl;
}

export function disconnectGoogle(): Promise<void> {
  return api<void>("/api/integrations/google/connection", { method: "DELETE" });
}
