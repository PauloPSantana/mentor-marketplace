export type StoredUser = {
  id?: string;
  name: string;
  email: string;
  role: "MENTOR" | "MENTEE" | "ADMIN" | string;
  photoUrl?: string | null;
  updatedAt?: string | null;
};

const USER_KEY = "mentorhub.user";
const TOKEN_KEY = "mentorhub.token";
export const AUTH_EVENT = "mentorhub:auth";

export function getStoredUser(): StoredUser | null {
  if (typeof window === "undefined") {
    return null;
  }
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as StoredUser;
  } catch {
    return null;
  }
}

export function setAuthSession(token: string, user: StoredUser): void {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  window.dispatchEvent(new Event(AUTH_EVENT));
}

export function updateStoredUser(user: StoredUser): void {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) {
    return;
  }
  setAuthSession(token, user);
}

export function clearAuthSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  window.dispatchEvent(new Event(AUTH_EVENT));
}

export function roleLabel(role: string): string {
  switch (role) {
    case "MENTOR":
      return "Mentor";
    case "MENTEE":
      return "Mentorado";
    case "INSTITUTION":
      return "Instituição";
    case "ADMIN":
      return "Admin";
    default:
      return role;
  }
}

export function dashboardPath(role: string): string {
  if (role === "MENTOR") return "/dashboard/mentor";
  if (role === "INSTITUTION") return "/dashboard/instituicao";
  return "/dashboard/mentorado";
}
