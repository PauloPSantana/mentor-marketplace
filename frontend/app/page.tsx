import Link from "next/link";

export default function HomePage() {
  return (
    <main>
      <section className="container" style={{ padding: "4rem 0 3rem" }}>
        <p style={{ color: "var(--accent)", fontWeight: 700, marginBottom: "0.75rem" }}>Marketplace de mentorias</p>
        <h1 style={{ fontSize: "clamp(2.4rem, 5vw, 4rem)", lineHeight: 1.05, maxWidth: 760, margin: "0 0 1rem" }}>
          MentorHub AI
        </h1>
        <p style={{ fontSize: "1.2rem", maxWidth: 620, marginBottom: "2rem" }}>
          Encontre o mentor certo para o seu próximo passo profissional.
        </p>
        <div style={{ display: "flex", gap: "0.75rem", flexWrap: "wrap" }}>
          <Link href="/mentorias" className="btn">Explorar mentorias</Link>
          <Link href="/cadastro" className="btn secondary">Sou mentor</Link>
        </div>
      </section>

      <section className="container" style={{ paddingBottom: "4rem" }}>
        <form action="/mentorias" style={{ display: "flex", gap: "0.75rem", maxWidth: 720 }}>
          <input
            className="input"
            name="q"
            placeholder="Ex.: Java, arquitetura, carreira, IA..."
            aria-label="Buscar mentorias"
          />
          <button className="btn" type="submit">Buscar</button>
        </form>
      </section>
    </main>
  );
}
