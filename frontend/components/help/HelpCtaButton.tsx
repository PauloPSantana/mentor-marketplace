"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { dashboardPath, getStoredUser } from "@/lib/auth";

type HelpCtaButtonProps = {
  label: string;
  href: string;
};

export function HelpCtaButton({ label, href }: HelpCtaButtonProps) {
  const [target, setTarget] = useState(href === "/dashboard" ? "/login" : href);

  useEffect(() => {
    if (href !== "/dashboard") {
      setTarget(href);
      return;
    }
    const user = getStoredUser();
    setTarget(user ? dashboardPath(user.role) : "/login");
  }, [href]);

  return (
    <p className="help-cta">
      <Link href={target} className="btn">
        {label}
      </Link>
    </p>
  );
}
