"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MentorStudyPlanPage() {
  return (
    <MentorshipClassroomHub
      title="Plano de Estudos"
      description="Abra a sala de cada mentorado para montar ou ajustar o plano."
      empty="Vincule um mentorado para criar o plano de estudos."
      tab="plano"
      actionLabel="Abrir plano"
      perspective="MENTOR"
    />
  );
}
