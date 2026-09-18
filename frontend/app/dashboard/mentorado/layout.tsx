"use client";

import { DashboardShell } from "@/components/dashboard/DashboardShell";

export default function MentoradoLayout({ children }: { children: React.ReactNode }) {
  return <DashboardShell role="MENTEE">{children}</DashboardShell>;
}
