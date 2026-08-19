const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export function mediaUrl(path: string | null | undefined, version?: string | number | null): string | null {
  if (!path) {
    return null;
  }
  const trimmed = path.trim();
  if (!trimmed) {
    return null;
  }
  const resolved = trimmed.startsWith("http://") || trimmed.startsWith("https://")
    ? trimmed
    : `${API_URL}${trimmed.startsWith("/") ? trimmed : `/${trimmed}`}`;
  if (!version) {
    return resolved;
  }
  const separator = resolved.includes("?") ? "&" : "?";
  return `${resolved}${separator}v=${encodeURIComponent(String(version))}`;
}
