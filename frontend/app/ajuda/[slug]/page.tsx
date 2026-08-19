import Link from "next/link";
import { notFound } from "next/navigation";
import { HelpArticle } from "@/components/help/HelpArticle";
import { HelpSidebar } from "@/components/help/HelpSidebar";
import { getHelpCategory, helpCategories } from "@/lib/help";

type AjudaArtigoPageProps = {
  params: Promise<{ slug: string }>;
};

export function generateStaticParams() {
  return helpCategories.map((category) => ({ slug: category.slug }));
}

export async function generateMetadata({ params }: AjudaArtigoPageProps) {
  const { slug } = await params;
  const category = getHelpCategory(slug);
  if (!category) {
    return { title: "Ajuda | MentorHub AI" };
  }
  return {
    title: `${category.title} | Central de Ajuda`,
    description: category.description
  };
}

export default async function AjudaArtigoPage({ params }: AjudaArtigoPageProps) {
  const { slug } = await params;
  const category = getHelpCategory(slug);
  if (!category) {
    notFound();
  }

  return (
    <main className="container help-page">
      <p className="post-meta">
        <Link href="/ajuda">← Central de Ajuda</Link>
      </p>
      <div className="help-layout">
        <HelpSidebar currentSlug={category.slug} />
        <HelpArticle category={category} />
      </div>
    </main>
  );
}
