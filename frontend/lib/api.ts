const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

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
    if (error.status === 401 || error.status === 403) {
      return "Sessão expirada. Faça login novamente.";
    }
    if (!error.status) {
      return "Não foi possível conectar à API. Verifique se o backend está rodando em http://localhost:8080.";
    }
  }
  return fallback;
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  if (!headers.has("Content-Type") && init?.body) {
    headers.set("Content-Type", "application/json");
  }

  if (typeof window !== "undefined") {
    const token = localStorage.getItem("mentorhub.token");
    if (token && !headers.has("Authorization")) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...init,
      headers
    });
  } catch {
    throw new ApiError("Network error");
  }

  if (!response.ok) {
    throw new ApiError(`API error: ${response.status}`, response.status);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}
