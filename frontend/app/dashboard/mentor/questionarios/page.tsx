"use client";

import { MentorshipClassroomHub } from "@/components/dashboard/MentorshipClassroomHub";

export default function MentorQuestionnairesPage() {
  return (
    <MentorshipClassroomHub
      title="Questionários"
      description="Questionários e check-ins de cada mentoria."
      empty="Nenhuma mentoria com questionários ainda."
      tab="questionarios"
      actionLabel="Abrir questionários"
      perspective="MENTOR"
    />
  );
}
