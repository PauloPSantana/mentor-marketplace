export function classroomPath(mentorshipId: string, tab?: string): string {
  return tab
    ? `/dashboard/mentorships/${mentorshipId}?tab=${tab}`
    : `/dashboard/mentorships/${mentorshipId}`;
}
