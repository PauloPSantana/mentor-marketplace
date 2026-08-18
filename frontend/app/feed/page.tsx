"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { CreatePost } from "@/components/feed/CreatePost";
import { PostCard } from "@/components/feed/PostCard";
import { apiErrorMessage } from "@/lib/api";
import { getStoredUser } from "@/lib/auth";
import { listPosts, type FeedType, type Post } from "@/lib/feed";

const FEED_TABS: { type: FeedType; label: string; description: string }[] = [
  {
    type: "FOR_YOU",
    label: "Para você",
    description: "Publicações recomendadas com base em quem você segue e nos seus interesses."
  },
  {
    type: "FOLLOWING",
    label: "Seguindo",
    description: "Publicações das pessoas que você segue."
  },
  {
    type: "RECENT",
    label: "Mais recentes",
    description: "Todas as publicações da comunidade, das mais recentes para as mais antigas."
  }
];

export default function FeedPage() {
  const router = useRouter();
  const [ready, setReady] = useState(false);
  const [currentUserId, setCurrentUserId] = useState<string | undefined>();
  const [feedType, setFeedType] = useState<FeedType>("FOR_YOU");
  const [posts, setPosts] = useState<Post[]>([]);
  const [last, setLast] = useState(false);
  const [initialLoading, setInitialLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const sentinelRef = useRef<HTMLDivElement | null>(null);
  const fetchingRef = useRef(false);
  const pageRef = useRef(0);
  const lastRef = useRef(false);
  const feedTypeRef = useRef<FeedType>("FOR_YOU");

  useEffect(() => {
    const user = getStoredUser();
    if (!user) {
      router.replace("/login");
      return;
    }
    setCurrentUserId(user.id);
    setReady(true);
  }, [router]);

  const loadPage = useCallback(async (nextPage: number, replace = false, type = feedTypeRef.current) => {
    if (fetchingRef.current) {
      return;
    }
    if (!replace && (lastRef.current || nextPage <= pageRef.current)) {
      return;
    }

    fetchingRef.current = true;
    if (replace) {
      setInitialLoading(true);
    } else {
      setLoadingMore(true);
    }
    setError(null);

    try {
      const feed = await listPosts(nextPage, 10, type);
      if (type !== feedTypeRef.current) {
        return;
      }
      setPosts((current) => {
        if (replace) {
          return feed.items;
        }
        const seen = new Set(current.map((item) => item.id));
        return [...current, ...feed.items.filter((item) => !seen.has(item.id))];
      });
      pageRef.current = feed.page;
      lastRef.current = feed.last;
      setLast(feed.last);
    } catch (error) {
      setError(apiErrorMessage(error, "Não foi possível carregar o feed."));
    } finally {
      fetchingRef.current = false;
      setInitialLoading(false);
      setLoadingMore(false);
    }
  }, []);

  const handleFeedTypeChange = (nextType: FeedType) => {
    if (nextType === feedType) {
      return;
    }
    feedTypeRef.current = nextType;
    setFeedType(nextType);
    setPosts([]);
    setLast(false);
    pageRef.current = 0;
    lastRef.current = false;
    setError(null);
  };

  useEffect(() => {
    if (ready) {
      pageRef.current = 0;
      lastRef.current = false;
      void loadPage(0, true, feedType);
    }
  }, [ready, feedType, loadPage]);

  useEffect(() => {
    if (!ready || last) {
      return;
    }
    const node = sentinelRef.current;
    if (!node) {
      return;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting && !fetchingRef.current && !lastRef.current) {
          void loadPage(pageRef.current + 1, false, feedTypeRef.current);
        }
      },
      { rootMargin: "120px" }
    );
    observer.observe(node);
    return () => observer.disconnect();
  }, [ready, last, posts.length, loadPage, feedType]);

  if (!ready) {
    return <main className="container feed-page">Carregando...</main>;
  }

  const activeTab = FEED_TABS.find((tab) => tab.type === feedType) ?? FEED_TABS[0];

  const emptyMessage =
    feedType === "FOLLOWING"
      ? "Você ainda não segue ninguém. Explore mentorias e siga perfis para ver publicações aqui."
      : feedType === "FOR_YOU"
        ? "Nenhuma publicação para recomendar ainda. Confira as mais recentes ou seja o primeiro a compartilhar."
        : "Nenhuma publicação ainda. Seja o primeiro a compartilhar.";

  const footerMessage = initialLoading
    ? "Carregando publicações..."
    : loadingMore
      ? "Carregando mais publicações..."
      : last && posts.length > 0
        ? "Você viu todas as publicações."
        : null;

  return (
    <main className="container feed-page">
      <h1>Feed</h1>
      <p className="feed-subtitle">{activeTab.description}</p>
      <div className="feed-tabs" role="tablist" aria-label="Tipo de feed">
        {FEED_TABS.map((tab) => (
          <button
            key={tab.type}
            type="button"
            role="tab"
            aria-selected={feedType === tab.type}
            className={feedType === tab.type ? "feed-tab feed-tab-active" : "feed-tab"}
            onClick={() => handleFeedTypeChange(tab.type)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <CreatePost onCreated={(post) => setPosts((current) => [post, ...current.filter((item) => item.id !== post.id)])} />
      {error ? <p className="error">{error}</p> : null}
      <section className="feed-list">
        {posts.map((post) => (
          <PostCard
            key={post.id}
            post={post}
            currentUserId={currentUserId}
            onUpdated={(updated) =>
              setPosts((current) => current.map((item) => (item.id === updated.id ? updated : item)))
            }
            onDeleted={(postId) => setPosts((current) => current.filter((item) => item.id !== postId))}
          />
        ))}
        {!error && !initialLoading && !loadingMore && posts.length === 0 ? (
          <p className="feed-empty">{emptyMessage}</p>
        ) : null}
      </section>
      <div ref={sentinelRef} className="feed-sentinel" aria-hidden="true" />
      <footer className="feed-footer" aria-live="polite">
        {footerMessage ? <p className="feed-status">{footerMessage}</p> : null}
      </footer>
    </main>
  );
}
