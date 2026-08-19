import type { HelpCategory, HelpStatusTone } from "@/lib/help";
import { HelpCtaButton } from "@/components/help/HelpCtaButton";

const STATUS_EMOJI: Record<HelpStatusTone, string> = {
  pending: "🟡",
  success: "🟢",
  danger: "🔴",
  muted: "⚪"
};

type HelpArticleProps = {
  category: HelpCategory;
};

export function HelpArticle({ category }: HelpArticleProps) {
  return (
    <article className="help-article">
      <p className="post-meta">
        {category.icon} {category.description}
      </p>
      <h1>{category.title}</h1>
      {category.sections.map((section) => (
        <section key={section.title} className="help-section">
          <h2>{section.title}</h2>
          {section.body ? <p>{section.body}</p> : null}
          {section.steps && section.steps.length > 0 ? (
            <ol className="help-steps">
              {section.steps.map((step) => (
                <li key={step}>{step}</li>
              ))}
            </ol>
          ) : null}
          {section.statuses && section.statuses.length > 0 ? (
            <ul className="help-status-list">
              {section.statuses.map((status) => (
                <li key={status.label} className={`help-status help-status-${status.tone}`}>
                  <strong>
                    <span aria-hidden="true">{STATUS_EMOJI[status.tone]} </span>
                    {status.label}
                  </strong>
                  <p className="post-meta">{status.description}</p>
                </li>
              ))}
            </ul>
          ) : null}
          {section.tips && section.tips.length > 0 ? (
            <ul className="help-tips">
              {section.tips.map((tip) => (
                <li key={tip}>{tip}</li>
              ))}
            </ul>
          ) : null}
        </section>
      ))}
      {category.cta ? <HelpCtaButton label={category.cta.label} href={category.cta.href} /> : null}
    </article>
  );
}
