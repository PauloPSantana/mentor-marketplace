"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MentorTasksPage() {
  return (
    <MentorshipClassroomHub
      title="Tarefas"
      description="Acompanhe as tarefas de cada mentoria."
      empty="Nenhuma mentoria para atribuir tarefas ainda."
      tab="tarefas"
      actionLabel="Abrir tarefas"
      perspective="MENTOR"
    />
  );
}
