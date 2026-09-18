"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MenteeStudyPlanPage() {
  return (
    <MentorshipClassroomHub
      title="Plano de Estudos"
      description="Acesse o plano combinado com o seu mentor."
      empty="Você ainda não possui uma mentoria com plano de estudos."
      tab="plano"
      actionLabel="Abrir plano"
      perspective="MENTEE"
    />
  );
}
