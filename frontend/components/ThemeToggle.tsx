"use client";

import { useEffect, useState } from "react";
import { getStoredTheme, toggleTheme, type Theme } from "@/lib/theme";

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>("night");
  const nightOn = theme === "night";

  useEffect(() => {
    setTheme(getStoredTheme());
  }, []);

  return (
    <button
      className={`theme-switch${nightOn ? " on" : ""}`}
      type="button"
      role="switch"
      aria-checked={nightOn}
      aria-label={nightOn ? "Desativar modo noite" : "Ativar modo noite"}
      title={nightOn ? "Modo noite" : "Modo dia"}
      onClick={() => setTheme(toggleTheme(theme))}
    >
      <span className="theme-switch-knob" />
    </button>
  );
}
