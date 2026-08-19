import Link from "next/link";
import type { HelpCategory } from "@/lib/help";

type HelpCategoryCardProps = {
  category: HelpCategory;
};

export function HelpCategoryCard({ category }: HelpCategoryCardProps) {
  return (
    <Link href={`/ajuda/${category.slug}`} className="help-category-card">
      <span className="help-category-icon" aria-hidden="true">
        {category.icon}
      </span>
      <span>
        <strong>{category.title}</strong>
        <span className="post-meta">{category.description}</span>
      </span>
    </Link>
  );
}
