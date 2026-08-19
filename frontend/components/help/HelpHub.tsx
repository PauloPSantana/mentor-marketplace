"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { HelpCategoryCard } from "@/components/help/HelpCategoryCard";
import { HelpSearch } from "@/components/help/HelpSearch";
import { searchHelp } from "@/lib/help";

export function HelpHub() {
  const router = useRouter();
  const [query, setQuery] = useState("");
  const results = useMemo(() => searchHelp(query), [query]);

  return (
    <div className="help-hub">
      <header className="help-hero">
        <p className="help-kicker">❓ Central de Ajuda</p>
        <h1>Aprenda a utilizar o Mentor Marketplace</h1>
        <HelpSearch
          value={query}
          onChange={setQuery}
          onSubmit={() => {
            if (results.length === 1) {
              router.push(`/ajuda/${results[0].slug}`);
            }
          }}
        />
      </header>
      {results.length === 0 ? (
        <p className="feed-empty">Nenhum tópico encontrado para “{query}”.</p>
      ) : (
        <section className="help-grid" aria-label="Tópicos de ajuda">
          {results.map((category) => (
            <HelpCategoryCard key={category.slug} category={category} />
          ))}
        </section>
      )}
    </div>
  );
}
