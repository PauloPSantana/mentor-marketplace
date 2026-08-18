"use client";

type LikeButtonProps = {
  liked: boolean;
  pending?: boolean;
  onToggle: () => void;
};

export function LikeButton({ liked, pending = false, onToggle }: LikeButtonProps) {
  return (
    <button
      className={`post-action${liked ? " liked" : ""}`}
      type="button"
      disabled={pending}
      aria-pressed={liked}
      onClick={onToggle}
    >
      👍 {liked ? "Curtido" : "Curtir"}
    </button>
  );
}
