import type { Metadata } from "next";
import { AppHeader } from "@/components/AppHeader";
import "./globals.css";

export const metadata: Metadata = {
  title: "MentorHub AI",
  description: "Marketplace inteligente de mentorias"
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR" data-theme="night" suppressHydrationWarning>
      <head>
        <script
          dangerouslySetInnerHTML={{
            __html: `try{var t=localStorage.getItem("mentorhub.theme");document.documentElement.dataset.theme=t==="day"?"day":"night"}catch(e){}`
          }}
        />
      </head>
      <body suppressHydrationWarning>
        <AppHeader />
        {children}
      </body>
    </html>
  );
}
