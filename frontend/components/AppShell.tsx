"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { api, clearSession, getUser, type User } from "@/lib/api";

const LINKS: Record<User["role"], { href: string; label: string }[]> = {
  customer: [
    { href: "/paketler", label: "Paketler" },
    { href: "/siparislerim", label: "Siparişlerim" },
    { href: "/takip", label: "Takip ve Bildirimler" },
  ],
  business: [{ href: "/isletme", label: "İşletme Paneli" }],
};

/**
 * Giris yapilmis sayfalarin ortak iskeleti.
 * Giris yoksa veya rol uymuyorsa giris sayfasina gonderir.
 */
export default function AppShell({
  role,
  children,
}: {
  role: User["role"];
  children: (user: User) => React.ReactNode;
}) {
  const router = useRouter();
  const path = usePathname();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const u = getUser();
    if (!u || u.role !== role) {
      router.replace("/");
      return;
    }
    setUser(u);
  }, [role, router]);

  async function logout() {
    try {
      await api("/logout", { method: "POST" });
    } catch {
      // oturum zaten dusmus olabilir
    }
    clearSession();
    router.replace("/");
  }

  if (!user) return null;

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-10 border-b border-line bg-cream/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-4">
          <Link href={LINKS[role][0].href} className="flex shrink-0 items-center gap-2 font-bold">
            <span className="grid size-8 place-items-center rounded-lg bg-sun-400 text-sm text-leaf-900">AS</span>
            <span className="hidden sm:inline">Artan Sepet</span>
          </Link>
          <nav className="flex gap-1 overflow-x-auto">
            {LINKS[role].map((l) => (
              <Link
                key={l.href}
                href={l.href}
                className={`whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium transition ${
                  path === l.href ? "bg-leaf-100 text-leaf-700" : "text-muted hover:bg-leaf-50 hover:text-ink"
                }`}
              >
                {l.label}
              </Link>
            ))}
          </nav>
          <div className="ml-auto flex shrink-0 items-center gap-3">
            <span className="hidden text-sm text-muted md:inline">{user.name}</span>
            <button
              onClick={logout}
              className="rounded-lg border border-line px-3 py-2 text-sm font-medium transition hover:bg-white"
            >
              Çıkış
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-6 py-8">{children(user)}</main>
    </div>
  );
}