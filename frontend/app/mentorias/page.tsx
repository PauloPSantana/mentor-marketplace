type SearchParams = Promise<{ q?: string }>;

export default async function MentoriasPage({ searchParams }: { searchParams: SearchParams }) {
  const params = await searchParams;
  const query = params.q?.trim();

  return (
    <main className="container" style={{ padding: "2.5rem 0 4rem" }}>
      <h1 style={{ marginBottom: "0.5rem" }}>Catálogo de mentorias</h1>
      <p style={{ marginBottom: "1.5rem", color: "#486581" }}>
        {query ? `Resultados para “${query}”.` : "Busca e filtros serão conectados à API pública."}
      </p>
      <div className="card-form">
        <p style={{ margin: 0 }}>
          O marketplace (Sprint 4) listará produtos publicados com paginação, filtros por categoria,
          nível, preço e avaliação.
        </p>
      </div>
    </main>
  );
}
