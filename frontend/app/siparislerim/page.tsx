"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { api, getUser, tl, type Order } from "@/lib/api";

// Java'daki durum adlari -> ekranda gorunen ad ve renk
const STATE: Record<string, { label: string; cls: string }> = {
  Rezerve: { label: "Rezerve", cls: "bg-sun-100 text-sun-700" },
  Odendi: { label: "Ödendi · hazırlanıyor", cls: "bg-leaf-100 text-leaf-700" },
  "Teslime Hazir": { label: "Teslime hazır", cls: "bg-leaf-600 text-white" },
  "Teslim Alindi": { label: "Teslim alındı", cls: "bg-gray-100 text-gray-600" },
  Iptal: { label: "İptal edildi", cls: "bg-red-50 text-red-700" },
  "Suresi Doldu": { label: "Süresi doldu", cls: "bg-gray-100 text-gray-600" },
};

const IPTAL_EDILEBILIR = ["Rezerve", "Odendi"];

export default function SiparislerimPage() {
  return <AppShell role="customer">{() => <Siparislerim />}</AppShell>;
}

function Siparislerim() {
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const showError = (e: unknown) => {
    if (!getUser()) location.href = "/";
    setError(e instanceof Error ? e.message : "Bir hata oluştu");
  };

  const load = useCallback(async () => {
    try {
      setOrders(await api<Order[]>("/orders"));
    } catch (e) {
      showError(e);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function cancel(o: Order) {
    if (!confirm(`"${o.bagTitle}" siparişini iptal etmek istiyor musun?`)) return;
    setBusy(o.id);
    setError(null);
    try {
      await api(`/orders/${o.id}/cancel`, { method: "POST" });
      await load();
    } catch (e) {
      showError(e);
    } finally {
      setBusy(null);
    }
  }

  const aktif = orders?.filter((o) => o.open) ?? [];
  const gecmis = orders?.filter((o) => !o.open) ?? [];

  return (
    <>
      <h1 className="text-3xl font-extrabold">Siparişlerim</h1>
      <p className="mt-1 text-muted">Teslim kodunu teslim alırken işletmeye söylemeyi unutma.</p>

      {error && (
        <p className="mt-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}

      {orders === null ? (
        <div className="mt-8 h-40 animate-pulse rounded-3xl bg-white" />
      ) : orders.length === 0 ? (
        <div className="mt-8 rounded-3xl border border-dashed border-line bg-white p-12 text-center">
          <p className="text-muted">Henüz siparişin yok.</p>
          <Link href="/paketler" className="mt-4 inline-block rounded-xl bg-leaf-600 px-5 py-2.5 font-semibold text-white">
            Paketlere göz at
          </Link>
        </div>
      ) : (
        <>
          <h2 className="mt-8 text-lg font-bold">Aktif ({aktif.length})</h2>
          <div className="mt-3 grid gap-4 md:grid-cols-2">
            {aktif.map((o) => (
              <article key={o.id} className="rounded-3xl border border-line bg-white p-5 shadow-sm">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-bold">{o.bagTitle}</h3>
                    <p className="text-sm text-muted">
                      {o.businessName} · {o.createdAt}
                    </p>
                  </div>
                  <StateBadge state={o.state} />
                </div>
                <div className="mt-4 flex items-end justify-between rounded-2xl bg-leaf-50 p-4">
                  <div>
                    <div className="text-xs text-muted">Teslim kodu · {o.pickupEnd}&apos;a kadar</div>
                    <div className="text-3xl font-extrabold tracking-[0.25em] text-leaf-700">{o.pickupCode}</div>
                  </div>
                  <div className="text-lg font-bold">{tl(o.price)}</div>
                </div>
                {IPTAL_EDILEBILIR.includes(o.state) && (
                  <button
                    onClick={() => cancel(o)}
                    disabled={busy === o.id}
                    className="mt-4 text-sm font-semibold text-red-700 hover:underline disabled:opacity-50"
                  >
                    {busy === o.id ? "İptal ediliyor..." : "Siparişi iptal et"}
                  </button>
                )}
              </article>
            ))}
            {aktif.length === 0 && <p className="text-sm text-muted">Aktif siparişin yok.</p>}
          </div>

          {gecmis.length > 0 && (
            <>
              <h2 className="mt-10 text-lg font-bold">Geçmiş ({gecmis.length})</h2>
              <div className="mt-3 divide-y divide-line overflow-hidden rounded-3xl border border-line bg-white">
                {gecmis.map((o) => (
                  <div key={o.id} className="flex items-center justify-between gap-3 p-4">
                    <div>
                      <div className="font-semibold">{o.bagTitle}</div>
                      <div className="text-sm text-muted">
                        {o.businessName} · {o.createdAt} · {tl(o.price)}
                      </div>
                    </div>
                    <StateBadge state={o.state} />
                  </div>
                ))}
              </div>
            </>
          )}
        </>
      )}
    </>
  );
}

function StateBadge({ state }: { state: string }) {
  const s = STATE[state] ?? { label: state, cls: "bg-gray-100 text-gray-600" };
  return <span className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${s.cls}`}>{s.label}</span>;
}