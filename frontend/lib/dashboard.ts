export type DashboardRole = "INSTITUTION" | "MENTOR" | "MENTEE";

export type DashboardNavItem = {
  href: string;
  label: string;
};

export function dashboardNav(role: DashboardRole): DashboardNavItem[] {
  if (role === "INSTITUTION") {
    return [
      { href: "/dashboard/instituicao", label: "Dashboard" },
      { href: "/dashboard/instituicao/mentores", label: "Mentores" },
      { href: "/dashboard/instituicao/mentorados", label: "Mentorados" },
      { href: "/dashboard/instituicao/programas", label: "Programas" },
      { href: "/dashboard/instituicao/agenda", label: "Agenda" },
      { href: "/dashboard/instituicao/convites", label: "Convites" },
      { href: "/dashboard/instituicao/mentorias", label: "Mentorias" },
      { href: "/dashboard/instituicao/relatorios", label: "Relatórios" },
      { href: "/dashboard/instituicao/configuracoes", label: "Configurações" }
    ];
  }
  if (role === "MENTOR") {
    return [
      { href: "/dashboard/mentor", label: "Dashboard" },
      { href: "/dashboard/mentor/mentorados", label: "Meus Mentorados" },
      { href: "/dashboard/mentor/agenda", label: "Agenda" },
      { href: "/dashboard/mentor/plano", label: "Plano de Estudos" },
      { href: "/dashboard/mentor/tarefas", label: "Tarefas" },
      { href: "/dashboard/mentor/questionarios", label: "Questionários" },
      { href: "/dashboard/mentor/mensagens", label: "Mensagens" },
      { href: "/dashboard/mentor/perfil", label: "Perfil" }
    ];
  }
  return [
    { href: "/dashboard/mentorado", label: "Dashboard" },
    { href: "/dashboard/mentorado/jornada", label: "Minha Jornada" },
    { href: "/dashboard/mentorado/agenda", label: "Agenda" },
    { href: "/dashboard/mentorado/plano", label: "Plano de Estudos" },
    { href: "/dashboard/mentorado/tarefas", label: "Tarefas" },
    { href: "/dashboard/mentorado/questionarios", label: "Questionários" },
    { href: "/dashboard/mentorado/progresso", label: "Progresso" }
  ];
}

export function startOfWeek(date = new Date()): Date {
  const value = new Date(date);
  const weekday = value.getDay();
  const offset = weekday === 0 ? -6 : 1 - weekday;
  value.setDate(value.getDate() + offset);
  value.setHours(0, 0, 0, 0);
  return value;
}

export function endOfWeek(date = new Date()): Date {
  const value = startOfWeek(date);
  value.setDate(value.getDate() + 7);
  return value;
}

export function agendaPath(role: string): string {
  if (role === "INSTITUTION") return "/dashboard/instituicao/agenda";
  if (role === "MENTOR") return "/dashboard/mentor/agenda";
  return "/dashboard/mentorado/agenda";
}

export function isSameDay(left: Date, right: Date): boolean {
  return left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();
}

export function startOfMonth(date = new Date()): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

export function addMonths(date: Date, amount: number): Date {
  return new Date(date.getFullYear(), date.getMonth() + amount, 1);
}
