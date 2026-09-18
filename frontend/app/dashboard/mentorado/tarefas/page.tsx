"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MenteeTasksPage() {
  return (
    <MentorshipClassroomHub
      title="Tarefas"
      description="Tarefas combinadas na sua mentoria."
      empty="Nenhuma tarefa disponível ainda."
      tab="tarefas"
      actionLabel="Abrir tarefas"
      perspective="MENTEE"
    />
  );
}
