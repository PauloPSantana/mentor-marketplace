import Link from "next/link";
import { helpCategories } from "@/lib/help";

type HelpSidebarProps = {
  currentSlug: string;
};

export function HelpSidebar({ currentSlug }: HelpSidebarProps) {
  return (
    <aside className="help-sidebar">
      <p className="help-sidebar-title">Tópicos</p>
      <nav aria-label="Tópicos da ajuda">
        <ul>
          {helpCategories.map((category) => (
            <li key={category.slug}>
              <Link
                href={`/ajuda/${category.slug}`}
                className={category.slug === currentSlug ? "help-nav-link active" : "help-nav-link"}
              >
                <span aria-hidden="true">{category.icon}</span>
                {category.title}
              </Link>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
