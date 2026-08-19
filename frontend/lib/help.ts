export type HelpStatusTone = "pending" | "success" | "danger" | "muted";

export type HelpStatus = {
  label: string;
  description: string;
  tone: HelpStatusTone;
};

export type HelpSection = {
  title: string;
  body?: string;
  steps?: string[];
  statuses?: HelpStatus[];
  tips?: string[];
};

export type HelpCta = {
  label: string;
  href: string;
};

export type HelpCategory = {
  slug: string;
  title: string;
  description: string;
  icon: string;
  keywords: string[];
  sections: HelpSection[];
  cta?: HelpCta;
};

export const helpCategories: HelpCategory[] = [
  {
    slug: "primeiros-passos",
    title: "Primeiros passos",
    description: "Cadastro, login e acesso",
    icon: "🚀",
    keywords: ["cadastro", "login", "conta", "senha", "começar"],
    sections: [
      {
        title: "Como criar sua conta",
        steps: [
          "Abra Criar conta.",
          "Informe nome, email e senha.",
          "A senha precisa ter 8 ou mais caracteres, com letras e números.",
          "Confirme a senha.",
          "Escolha Mentorado ou Mentor.",
          "Clique em Cadastrar e depois entre com email e senha."
        ]
      },
      {
        title: "Depois do login",
        body: "Mentorados vão para o painel de solicitações. Mentores vão para o painel de perfil, pedidos recebidos e mentorias ativas."
      }
    ],
    cta: { label: "Criar conta", href: "/cadastro" }
  },
  {
    slug: "perfil",
    title: "Meu Perfil",
    description: "Configure seu perfil",
    icon: "👤",
    keywords: ["nome", "foto", "linkedin", "bio", "headline"],
    sections: [
      {
        title: "Nome da conta",
        body: "No painel, clique no lápis ao lado do seu nome para abrir o modal de edição. O nome aparece no feed, nas solicitações e no topo da página."
      },
      {
        title: "Foto de perfil",
        body: "No feed, clique no círculo do avatar e envie uma imagem JPG, PNG ou WEBP de até 2 MB. A foto passa a aparecer nas suas publicações e no perfil de mentor."
      },
      {
        title: "Perfil profissional do mentor",
        body: "Mentores completam título, bio, especialidades, tecnologias, LinkedIn, GitHub, valor da sessão e modalidade no Dashboard do mentor. Marque o perfil como ativo para aparecer no catálogo."
      }
    ],
    cta: { label: "Abrir meu painel", href: "/dashboard" }
  },
  {
    slug: "feed",
    title: "Feed",
    description: "Posts e interações",
    icon: "📰",
    keywords: ["post", "publicar", "curtir", "comentar", "para você"],
    sections: [
      {
        title: "Abas do feed",
        body: "Para você mostra recomendações. Seguindo mostra quem você acompanha. Mais recentes lista todas as publicações da comunidade."
      },
      {
        title: "Como publicar",
        steps: [
          "Escreva sua ideia no campo No que você está pensando?",
          "Se quiser, informe a URL de uma imagem.",
          "Clique em Publicar.",
          "Você pode editar ou excluir as suas próprias publicações."
        ]
      },
      {
        title: "Interações",
        body: "Curtir, comentar, responder e compartilhar o link da publicação. Também é possível seguir o autor direto no card."
      }
    ],
    cta: { label: "Ir para o feed", href: "/feed" }
  },
  {
    slug: "mentores",
    title: "Seguir Mentores",
    description: "Construa sua rede",
    icon: "👥",
    keywords: ["seguir", "catálogo", "bloquear", "rede"],
    sections: [
      {
        title: "Encontrar mentores",
        steps: [
          "Abra Mentorias no menu.",
          "Explore o catálogo de perfis.",
          "Abra o perfil para ver bio, tecnologias e avaliações.",
          "Use Seguir para acompanhar as publicações no feed."
        ]
      },
      {
        title: "Bloquear",
        body: "No perfil, você pode bloquear um usuário. Isso impede interações indesejadas na rede."
      }
    ],
    cta: { label: "Ver catálogo", href: "/mentorias" }
  },
  {
    slug: "mentorias",
    title: "Mentorias",
    description: "Solicitações e acompanhamento",
    icon: "🎓",
    keywords: ["solicitar", "pedido", "aceita", "recusada", "pendente"],
    sections: [
      {
        title: "Como solicitar uma mentoria",
        steps: [
          "Encontre um mentor no catálogo.",
          "Abra o perfil do profissional.",
          "Escolha o serviço desejado.",
          "Clique em Solicitar mentoria.",
          "Escreva uma mensagem explicando seu objetivo.",
          "Envie a solicitação."
        ]
      },
      {
        title: "Status da solicitação",
        statuses: [
          { label: "Pendente", description: "Aguardando resposta do mentor.", tone: "pending" },
          { label: "Aceita", description: "O mentor aceitou sua solicitação.", tone: "success" },
          { label: "Recusada", description: "O mentor recusou a solicitação.", tone: "danger" },
          { label: "Cancelada", description: "Você cancelou a solicitação.", tone: "muted" }
        ]
      },
      {
        title: "Para mentores",
        body: "Os pedidos aparecem no Dashboard do mentor. Você pode aceitar ou recusar. Depois de aceito, a mentoria fica disponível para agenda, pagamento e sessões."
      }
    ],
    cta: { label: "Ver minhas mentorias", href: "/dashboard" }
  },
  {
    slug: "agenda",
    title: "Agenda",
    description: "Sessões e horários",
    icon: "📅",
    keywords: ["sessão", "horário", "agendar", "reunião"],
    sections: [
      {
        title: "Onde ver seus horários",
        body: "A página Agenda lista as sessões das suas mentorias no seu fuso horário."
      },
      {
        title: "Como agendar",
        steps: [
          "Abra uma mentoria ativa no painel.",
          "Se houver cobrança, o mentorado precisa confirmar o pagamento antes.",
          "Informe data, horário, duração e o link da reunião.",
          "Crie a sessão. Depois ela pode ser concluída ou marcada como falta."
        ]
      }
    ],
    cta: { label: "Abrir agenda", href: "/agenda" }
  },
  {
    slug: "pagamentos",
    title: "Pagamentos",
    description: "Cobranças e pagamentos",
    icon: "💳",
    keywords: ["pagar", "cobrança", "valor", "sessão"],
    sections: [
      {
        title: "Quando pagar",
        body: "Se o mentor definiu um valor de sessão maior que zero, o mentorado confirma o pagamento na página da mentoria depois que o pedido é aceito. Sem o pagamento, as sessões ficam bloqueadas."
      },
      {
        title: "Status do pagamento",
        statuses: [
          { label: "Pendente", description: "Aguardando confirmação.", tone: "pending" },
          { label: "Pago", description: "Pagamento confirmado. As sessões podem ser agendadas.", tone: "success" },
          { label: "Falhou", description: "A tentativa não foi concluída. Tente novamente.", tone: "danger" }
        ]
      }
    ],
    cta: { label: "Abrir meu painel", href: "/dashboard" }
  },
  {
    slug: "avaliacoes",
    title: "Avaliações",
    description: "Avalie sua experiência",
    icon: "⭐",
    keywords: ["nota", "review", "estrelas", "feedback"],
    sections: [
      {
        title: "Quando avaliar",
        body: "Depois que a mentoria é concluída, mentor e mentorado podem enviar uma avaliação com nota de 1 a 5 e um comentário opcional."
      },
      {
        title: "Regras",
        tips: [
          "Cada pessoa avalia uma vez por mentoria.",
          "A avaliação pode ser editada por até 72 horas.",
          "A média do mentor considera apenas avaliações ativas e aparece no perfil público."
        ]
      }
    ],
    cta: { label: "Ver catálogo de mentores", href: "/mentorias" }
  },
  {
    slug: "notificacoes",
    title: "Notificações",
    description: "Acompanhe novidades",
    icon: "🔔",
    keywords: ["sino", "alerta", "pedido", "aceite"],
    sections: [
      {
        title: "Onde aparecem",
        body: "O sino no topo mostra quantas notificações ainda não foram lidas. Abra Notificações para ver pedidos, aceites, comentários e outras atualizações."
      },
      {
        title: "Marcar como lidas",
        body: "Você pode marcar todas como lidas na própria página de notificações."
      }
    ],
    cta: { label: "Ver notificações", href: "/notifications" }
  },
  {
    slug: "seguranca",
    title: "Segurança",
    description: "Proteja sua conta",
    icon: "🛡️",
    keywords: ["senha", "conta", "bloquear", "privacidade"],
    sections: [
      {
        title: "Boas práticas",
        tips: [
          "Use uma senha forte, com letras e números.",
          "Não compartilhe seu acesso com outras pessoas.",
          "Saia da conta ao usar um computador compartilhado.",
          "Mentores só editam o próprio perfil. Mentorados não criam produtos de mentoria.",
          "Use bloquear se alguém estiver incomodando na rede."
        ]
      }
    ],
    cta: { label: "Criar conta segura", href: "/cadastro" }
  }
];

export function getHelpCategory(slug: string): HelpCategory | undefined {
  return helpCategories.find((category) => category.slug === slug);
}

export function searchHelp(query: string): HelpCategory[] {
  const normalized = query.trim().toLowerCase();
  if (!normalized) {
    return helpCategories;
  }
  return helpCategories.filter((category) => {
    const haystack = [
      category.title,
      category.description,
      ...category.keywords,
      ...category.sections.flatMap((section) => [
        section.title,
        section.body ?? "",
        ...(section.steps ?? []),
        ...(section.tips ?? []),
        ...(section.statuses ?? []).map((status) => `${status.label} ${status.description}`)
      ])
    ]
      .join(" ")
      .toLowerCase();
    return haystack.includes(normalized);
  });
}
