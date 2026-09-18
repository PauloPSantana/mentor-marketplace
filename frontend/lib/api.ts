export function apiBaseUrl(): string {
  if (typeof window !== "undefined") {
    const host = window.location.hostname;
    if (host && host !== "localhost" && host !== "127.0.0.1") {
      return `http://${host}:8080`;
    }
  }
  return process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
}

export class ApiError extends Error {
  status?: number;

  constructor(message: string, status?: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof ApiError) {
    if (!error.status) {
      return "Não foi possível conectar à API. Verifique se o backend está rodando em http://localhost:8080.";
    }
    if (error.message && !error.message.startsWith("API error:")) {
      return error.message;
    }
    if (error.status === 401 || error.status === 403) {
      return fallback;
    }
  }
  return fallback;
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (!headers.has("Content-Type") && init?.body && !(init.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (typeof window !== "undefined") {
    const token = localStorage.getItem("mentorhub.token");
    const isPublicAuth =
      path === "/api/v1/auth/register" ||
      path === "/api/v1/auth/login" ||
      path.startsWith("/api/v1/auth/linkedin/") ||
      path.startsWith("/api/v1/invitations/") ||
      path.startsWith("/api/v1/mentor-invitations/");
    if (token && !headers.has("Authorization") && !isPublicAuth) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  let response: Response;
  try {
    response = await fetch(`${apiBaseUrl()}${path}`, {
      ...init,
      headers
    });
  } catch {
    throw new ApiError("Network error");
  }

  if (!response.ok) {
    let message = `API error: ${response.status}`;
    try {
      const body = (await response.json()) as { message?: string };
      if (body?.message) {
        message = body.message;
      }
    } catch {
      // keep generic message
    }
    throw new ApiError(message, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

export async function apiUpload<T>(path: string, file: File, fieldName = "file"): Promise<T> {
  const body = new FormData();
  body.append(fieldName, file);
  return api<T>(path, {
    method: "POST",
    body
  });
}
