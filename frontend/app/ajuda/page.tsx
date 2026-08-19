import { HelpHub } from "@/components/help/HelpHub";

export const metadata = {
  title: "Central de Ajuda | MentorHub AI",
  description: "Aprenda a utilizar o Mentor Marketplace"
};

export default function AjudaPage() {
  return (
    <main className="container help-page">
      <HelpHub />
    </main>
  );
}
