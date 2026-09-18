"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { agendaPath } from "@/lib/dashboard";
import { getStoredUser } from "@/lib/auth";

export default function AgendaRedirectPage() {
  const router = useRouter();

  useEffect(() => {
    const user = getStoredUser();
    if (!user) {
      router.replace("/login");
      return;
    }
    router.replace(agendaPath(user.role));
  }, [router]);

  return <main className="container" style={{ padding: "3rem 0" }}>Redirecionando para a agenda...</main>;
}
