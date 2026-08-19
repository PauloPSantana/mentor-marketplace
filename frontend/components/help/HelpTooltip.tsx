"use client";

import Link from "next/link";
import { useEffect, useId, useRef, useState } from "react";

type HelpTooltipProps = {
  text: string;
  href?: string;
  label?: string;
};

export function HelpTooltip({ text, href = "/ajuda", label = "Ajuda" }: HelpTooltipProps) {
  const tooltipId = useId();
  const rootRef = useRef<HTMLSpanElement>(null);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    if (!open) {
      return;
    }
    function onPointerDown(event: MouseEvent) {
      if (rootRef.current && !rootRef.current.contains(event.target as Node)) {
        setOpen(false);
      }
    }
    function onKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setOpen(false);
      }
    }
    window.addEventListener("mousedown", onPointerDown);
    window.addEventListener("keydown", onKeyDown);
    return () => {
      window.removeEventListener("mousedown", onPointerDown);
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [open]);

  return (
    <span className="help-tooltip" ref={rootRef}>
      <button
        type="button"
        className="help-tooltip-btn"
        aria-expanded={open}
        aria-controls={tooltipId}
        aria-label={label}
        title={label}
        onClick={() => setOpen((current) => !current)}
      >
        ?
      </button>
      {open ? (
        <span className="help-tooltip-card" id={tooltipId} role="tooltip">
          <span>{text}</span>
          <Link href={href} className="help-tooltip-link" onClick={() => setOpen(false)}>
            Ver no manual
          </Link>
        </span>
      ) : null}
    </span>
  );
}
