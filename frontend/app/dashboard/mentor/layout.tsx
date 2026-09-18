"use client";

import { DashboardShell } from "@/components/dashboard/DashboardShell";

export default function MentorLayout({ children }: { children: React.ReactNode }) {
  return <DashboardShell role="MENTOR">{children}</DashboardShell>;
}
