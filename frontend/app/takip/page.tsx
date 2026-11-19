"use client";

import { useCallback, useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { api, getUser, type Business } from "@/lib/api";

const TYPE_TR: Record<string, string> = {
  BAKERY: "🥖 Fırın",
  PASTRY: "🍰 Pastane",
  CAFE: "☕ Kafe",
  RESTAURANT: "🍲 Restoran",
  MARKET: "🛒 Market",
  GREENGROCER: "🥕 Manav",
  HOTEL: "🏨 Otel",
};

export default function TakipPage() {
  return <AppShell role="customer">{() => <Takip />}</AppShell>;
}

function Takip() {
  const [businesses, setBusinesses] = useState<Business[] | null>(null);
  const [notifications, setNotifications] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const [b, n] = await Promise.all([api<Business[]>("/businesses"), api<string[]>("/notifications")]);
      setBusinesses(b);
      setNotifications([...n].reverse()); // en yeni en ustte
    } catch (e) {
      if (!getUser()) location.href = "/";
      setError(e instanceof Error ? e.message : "Bir hata oluştu");
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function toggle(b: Business) {
    try {
      const updated = await api<Business>(`/businesses/${b.id}/${b.followed ? "unfollow" : "follow"}`, {
        method: "POST",
      });
      setBusinesses((list) => list?.map((x) => (x.id === updated.id ? updated : x)) ?? null);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Bir hata oluştu");
    }
  }

  return (
    <>
      <h1 className="text-3xl font-extrabold">Takip ve Bildirimler</h1>
      <p className="mt-1 text-muted">Sevdiğin işletmeleri takip et, yeni paket eklediklerinde ilk sen haberdar ol.</p>

      {error && (
        <p className="mt-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}

      <div className="mt-8 grid gap-8 lg:grid-cols-[1.3fr_1fr]">
        <section>
          <h2 className="text-lg font-bold">İşletmeler</h2>
          <div className="mt-3 space-y-3">
            {businesses === null
              ? [1, 2, 3].map((i) => <div key={i} className="h-20 animate-pulse rounded-2xl bg-white" />)
              : businesses.map((b) => (
                  <div
                    key={b.id}
                    className="flex items-center justify-between gap-4 rounded-2xl border border-line bg-white p-4"
                  >
                    <div>
                      <div className="font-semibold">{b.name}</div>
                      <div className="text-sm text-muted">
                        {TYPE_TR[b.type] ?? b.type} · {b.address}
                      </div>
                      <div className="text-xs text-muted">{b.followerCount} takipçi</div>
                    </div>
                    <button
                      onClick={() => toggle(b)}
                      className={`shrink-0 rounded-xl px-4 py-2 text-sm font-semibold transition ${
                        b.followed
                          ? "border border-leaf-600 text-leaf-700 hover:bg-leaf-50"
                          : "bg-leaf-600 text-white hover:bg-leaf-700"
                      }`}
                    >
                      {b.followed ? "Takip ediliyor ✓" : "Takip et"}
                    </button>
                  </div>
                ))}
          </div>
        </section>

        <section>
          <h2 className="text-lg font-bold">Bildirimler ({notifications.length})</h2>
          <div className="mt-3 space-y-3">
            {notifications.length === 0 ? (
              <p className="rounded-2xl border border-dashed border-line bg-white p-6 text-center text-sm text-muted">
                Henüz bildirimin yok.
              </p>
            ) : (
              notifications.map((n, i) => (
                <div key={i} className="flex gap-3 rounded-2xl bg-white p-4 shadow-sm">
                  <span className="mt-1.5 size-2 shrink-0 rounded-full bg-sun-500" />
                  <p className="text-sm">{n}</p>
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </>
  );
}