"use client";

import { DashboardShell } from "@/components/dashboard/DashboardShell";

export default function InstitutionLayout({ children }: { children: React.ReactNode }) {
  return (
    <DashboardShell role="INSTITUTION">
      {children}
    </DashboardShell>
  );
}
