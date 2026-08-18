import type { Metadata } from "next";
import { AppHeader } from "@/components/AppHeader";
import "./globals.css";

export const metadata: Metadata = {
  title: "MentorHub AI",
  description: "Marketplace inteligente de mentorias"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" suppressHydrationWarning>
      <body suppressHydrationWarning>
        <AppHeader />
        {children}
      </body>
    </html>
  );
}
