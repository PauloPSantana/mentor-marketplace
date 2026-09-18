import { api } from "@/lib/api";

export type ZoomStatus = {
  oauthEnabled: boolean;
  accountMeetingsEnabled: boolean;
  connected: boolean;
};

export function getZoomStatus(): Promise<ZoomStatus> {
  return api<ZoomStatus>("/api/v1/zoom/status");
}

export async function connectZoom(): Promise<void> {
  const response = await api<{ authorizationUrl: string }>("/api/v1/zoom/connect");
  window.location.href = response.authorizationUrl;
}

export function disconnectZoom(): Promise<void> {
  return api<void>("/api/v1/zoom/connection", { method: "DELETE" });
}
