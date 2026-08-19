"use client";

import { FormEvent } from "react";

type HelpSearchProps = {
  value: string;
  onChange: (value: string) => void;
  onSubmit?: () => void;
};

export function HelpSearch({ value, onChange, onSubmit }: HelpSearchProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit?.();
  }

  return (
    <form className="help-search" onSubmit={handleSubmit} role="search">
      <label className="help-search-label">
        <span className="help-search-icon" aria-hidden="true">🔎</span>
        <input
          className="input"
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder="Pesquise uma dúvida..."
          aria-label="Pesquisar na central de ajuda"
        />
      </label>
    </form>
  );
}
