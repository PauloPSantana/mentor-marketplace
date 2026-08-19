const THEME_KEY = "mentorhub.theme";

export type Theme = "day" | "night";

export function getStoredTheme(): Theme {
  if (typeof window === "undefined") {
    return "night";
  }
  return window.localStorage.getItem(THEME_KEY) === "day" ? "day" : "night";
}

export function applyTheme(theme: Theme) {
  document.documentElement.dataset.theme = theme;
  window.localStorage.setItem(THEME_KEY, theme);
}

export function toggleTheme(current: Theme): Theme {
  const next = current === "night" ? "day" : "night";
  applyTheme(next);
  return next;
}
