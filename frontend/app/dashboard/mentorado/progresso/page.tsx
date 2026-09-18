"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MenteeProgressPage() {
  return (
    <MentorshipClassroomHub
      title="Progresso"
      description="Acompanhe a evolução da sua mentoria."
      empty="Você ainda não possui uma mentoria para acompanhar o progresso."
      tab="evolucao"
      actionLabel="Ver progresso"
      perspective="MENTEE"
    />
  );
}
