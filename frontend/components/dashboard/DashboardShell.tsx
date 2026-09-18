"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { ReactNode, useEffect, useState } from "react";
import { dashboardNav, type DashboardRole } from "@/lib/dashboard";
import { dashboardPath, getStoredUser, type StoredUser } from "@/lib/auth";

type DashboardShellProps = {
  role: DashboardRole;
  children: ReactNode;
};

export function DashboardShell({ role, children }: DashboardShellProps) {
  const router = useRouter();
  const pathname = usePathname();
  const [user, setUser] = useState<StoredUser | null>(null);

  useEffect(() => {
    const parsed = getStoredUser();
    if (!parsed) {
      router.replace("/login");
      return;
    }
    if (parsed.role !== role) {
      router.replace(dashboardPath(parsed.role));
      return;
    }
    setUser(parsed);
  }, [role, router]);

  if (!user) {
    return <main className="container" style={{ padding: "3rem 0" }}>Carregando...</main>;
  }

  const items = dashboardNav(role);

  return (
    <div className="dashboard-shell">
      <aside className="dashboard-sidebar">
        <p className="dashboard-sidebar-kicker">Painel</p>
        <nav className="dashboard-nav">
          {items.map((item) => {
            const active = item.href === pathname
              || (item.href !== dashboardPath(role) && pathname.startsWith(`${item.href}/`));
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`dashboard-nav-link${active ? " active" : ""}`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>
      </aside>
      <div className="dashboard-content">{children}</div>
    </div>
  );
}
