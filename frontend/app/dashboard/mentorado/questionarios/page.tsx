"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MenteeQuestionnairesPage() {
  return (
    <MentorshipClassroomHub
      title="Questionários"
      description="Questionários e check-ins da sua jornada."
      empty="Nenhum questionário disponível ainda."
      tab="questionarios"
      actionLabel="Abrir questionários"
      perspective="MENTEE"
    />
  );
}
