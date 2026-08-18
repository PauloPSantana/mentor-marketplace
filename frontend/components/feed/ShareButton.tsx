"use client";

type ShareButtonProps = {
  disabled?: boolean;
};

export function ShareButton({ disabled = true }: ShareButtonProps) {
  return (
    <button className="post-action" type="button" disabled={disabled} title="Compartilhamento na próxima etapa">
      ↗ Compartilhar
    </button>
  );
}
