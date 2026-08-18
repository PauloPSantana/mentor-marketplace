"use client";

type CommentButtonProps = {
  active?: boolean;
  onClick: () => void;
};

export function CommentButton({ active = false, onClick }: CommentButtonProps) {
  return (
    <button
      className={`post-action${active ? " active" : ""}`}
      type="button"
      onClick={onClick}
    >
      💬 Comentar
    </button>
  );
}
