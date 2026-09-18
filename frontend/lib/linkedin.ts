import { api, apiBaseUrl } from "@/lib/api";

export type LinkedInPreview = {
  url: string;
  username: string;
  suggestedName: string;
};

export type LinkedInImportedProfile = {
  name: string;
  email: string;
  pictureUrl: string | null;
  linkedinUrl: string | null;
};

export type PasswordChecks = {
  length: boolean;
  letter: boolean;
  digit: boolean;
  match: boolean;
};

const LINKEDIN_PROFILE = /^https?:\/\/(?:[\w-]+\.)?linkedin\.com\/in\/([A-Za-z0-9\-_%]+)(?:\/.*)?(?:\?.*)?$/i;

export function parseLinkedInUrl(rawUrl: string): LinkedInPreview {
  const trimmed = rawUrl.trim();
  const match = trimmed.match(LINKEDIN_PROFILE);
  if (!match) {
    throw new Error("Use um link válido do LinkedIn, por exemplo https://www.linkedin.com/in/seu-perfil");
  }
  const username = decodeURIComponent(match[1]).replaceAll("%20", "-");
  const suggestedName = username
    .split(/[-_]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1).toLowerCase())
    .join(" ");
  return {
    url: `https://www.linkedin.com/in/${username}`,
    username,
    suggestedName
  };
}

export async function previewLinkedInProfile(url: string): Promise<LinkedInPreview> {
  const local = parseLinkedInUrl(url);
  try {
    return await api<LinkedInPreview>("/api/v1/auth/linkedin/preview", {
      method: "POST",
      body: JSON.stringify({ url })
    });
  } catch {
    return local;
  }
}

export function getLinkedInStatus(): Promise<{ enabled: boolean }> {
  return api<{ enabled: boolean }>("/api/v1/auth/linkedin/status");
}

export function getLinkedInImportedProfile(token: string): Promise<LinkedInImportedProfile> {
  return api<LinkedInImportedProfile>(`/api/v1/auth/linkedin/import/${token}`);
}

export function linkedInStartUrl(): string {
  return `${apiBaseUrl()}/api/v1/auth/linkedin/start`;
}

export function validatePassword(password: string, confirmation: string): PasswordChecks {
  return {
    length: password.length >= 8 && password.length <= 72,
    letter: /[A-Za-zÀ-ÿ]/.test(password),
    digit: /\d/.test(password),
    match: confirmation.length > 0 && password === confirmation
  };
}

export function isPasswordValid(checks: PasswordChecks): boolean {
  return checks.length && checks.letter && checks.digit && checks.match;
}
