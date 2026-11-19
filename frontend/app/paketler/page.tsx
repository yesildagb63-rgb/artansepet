"use client";

import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import AppShell from "@/components/AppShell";
import { api, getUser, tl, type Bag, type Order } from "@/lib/api";

const SORTS = [
  { key: "price", label: "En ucuz" },
  { key: "distance", label: "En yakın" },
  { key: "time", label: "Yakında bitecek" },
];

const TAG_TR: Record<string, string> = {
  VEGAN: "Vegan",
  VEGETARIAN: "Vejetaryen",
  GLUTEN_FREE: "Glutensiz",
  HALAL: "Helal",
};

const TYPE_ICON: Record<string, string> = {
  BAKERY: "🥖",
  PASTRY: "🍰",
  CAFE: "☕",
  RESTAURANT: "🍲",
  MARKET: "🛒",
  GREENGROCER: "🥕",
  HOTEL: "🏨",
};

// Java tarafinda AppContext'te secili odeme adaptoru (Adapter pattern)
const PAYMENT_PROVIDER = "PayTR";

export default function PaketlerPage() {
  return <AppShell role="customer">{() => <Paketler />}</AppShell>;
}

function Paketler() {
  const [sort, setSort] = useState("price");
  const [bags, setBags] = useState<Bag[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [checkout, setCheckout] = useState<Bag | null>(null); // odeme ekranindaki paket
  const [paying, setPaying] = useState(false);
  const [payError, setPayError] = useState<string | null>(null);
  const [placed, setPlaced] = useState<Order | null>(null);

  const load = useCallback(async () => {
    try {
      setBags(await api<Bag[]>(`/bags?sort=${sort}`));
    } catch (e) {
      if (!getUser()) location.href = "/";
      setError(e instanceof Error ? e.message : "Bir hata oluştu");
    }
  }, [sort]);

  useEffect(() => {
    load();
  }, [load]);

  function openCheckout(bag: Bag) {
    setPayError(null);
    setCheckout(bag);
  }

  async function pay() {
    if (!checkout) return;
    setPaying(true);
    setPayError(null);
    try {
      // OrderFacade.placeOrder: stok + teslim kodu + odeme + bildirim tek cagrida
      const order = await api<Order>("/orders", { method: "POST", body: { bagId: checkout.id } });
      setCheckout(null);
      setPlaced(order);
      await load();
    } catch (e) {
      if (!getUser()) location.href = "/";
      setPayError(e instanceof Error ? e.message : "Ödeme alınamadı");
      await load(); // stok degismis olabilir
    } finally {
      setPaying(false);
    }
  }

  return (
    <>
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-extrabold">Bugünün sürpriz paketleri</h1>
          <p className="mt-1 text-muted">Gün sonu kalan lezzetler, israf olmadan sana gelsin.</p>
        </div>
        <div className="flex rounded-xl bg-leaf-50 p-1">
          {SORTS.map((s) => (
            <button
              key={s.key}
              onClick={() => setSort(s.key)}
              className={`rounded-lg px-3 py-2 text-sm font-semibold transition ${
                sort === s.key ? "bg-white text-leaf-700 shadow-sm" : "text-muted hover:text-ink"
              }`}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      {error && (
        <p className="mt-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}

      {bags === null ? (
        <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3].map((i) => (
            <div key={i} className="h-72 animate-pulse rounded-3xl bg-white" />
          ))}
        </div>
      ) : bags.length === 0 ? (
        <div className="mt-8 rounded-3xl border border-dashed border-line bg-white p-12 text-center text-muted">
          Şu an müsait paket yok. Takip ettiğin işletmeler paket ekleyince haber vereceğiz.
        </div>
      ) : (
        <div className="mt-8 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {bags.map((b) => (
            <BagCard key={b.id} bag={b} onOrder={() => openCheckout(b)} />
          ))}
        </div>
      )}

      {checkout && (
        <CheckoutModal
          bag={checkout}
          paying={paying}
          error={payError}
          onPay={pay}
          onClose={() => !paying && setCheckout(null)}
        />
      )}

      {placed && <SuccessModal order={placed} onClose={() => setPlaced(null)} />}
    </>
  );
}

function Modal({ children, onClose }: { children: React.ReactNode; onClose: () => void }) {
  return (
    <div className="fixed inset-0 z-20 grid place-items-center bg-ink/40 p-6 backdrop-blur-sm" onClick={onClose}>
      <div className="w-full max-w-md rounded-3xl bg-white p-7 shadow-2xl" onClick={(e) => e.stopPropagation()}>
        {children}
      </div>
    </div>
  );
}

function CheckoutModal({
  bag,
  paying,
  error,
  onPay,
  onClose,
}: {
  bag: Bag;
  paying: boolean;
  error: string | null;
  onPay: () => void;
  onClose: () => void;
}) {
  const indirim = bag.originalPrice - bag.currentPrice;
  return (
    <Modal onClose={onClose}>
      <div className="flex items-center justify-between">
        <h2 className="text-xl font-bold">Ödeme</h2>
        <span className="rounded-full bg-leaf-50 px-3 py-1 text-xs font-semibold text-leaf-700">Adım 2 / 2</span>
      </div>

      {/* Siparis ozeti */}
      <div className="mt-5 flex gap-4 rounded-2xl border border-line p-4">
        <div className="grid size-14 shrink-0 place-items-center rounded-xl bg-linear-to-br from-leaf-100 to-sun-100 text-3xl">
          {TYPE_ICON[bag.businessType] ?? "🛍️"}
        </div>
        <div className="min-w-0">
          <div className="font-semibold">{bag.title}</div>
          <div className="text-sm text-muted">{bag.businessName}</div>
          <div className="text-sm text-muted">
            Teslim: bugün {bag.pickupStart}–{bag.pickupEnd}
          </div>
        </div>
      </div>

      <dl className="mt-4 space-y-2 text-sm">
        <div className="flex justify-between">
          <dt className="text-muted">Normal fiyat</dt>
          <dd>{tl(bag.originalPrice)}</dd>
        </div>
        {indirim > 0 && (
          <div className="flex justify-between text-sun-700">
            <dt>İndirim</dt>
            <dd>-{tl(indirim)}</dd>
          </div>
        )}
        <div className="flex justify-between border-t border-line pt-2 text-base font-bold">
          <dt>Toplam</dt>
          <dd className="text-leaf-700">{tl(bag.currentPrice)}</dd>
        </div>
      </dl>

      {/* Demo kart: gercek kart bilgisi istenmez */}
      <div className="mt-5 rounded-2xl bg-linear-to-br from-leaf-700 to-leaf-900 p-5 text-white">
        <div className="flex items-center justify-between text-xs uppercase tracking-wider text-leaf-100">
          <span>Demo kart</span>
          <span>{PAYMENT_PROVIDER}</span>
        </div>
        <div className="mt-6 font-mono text-lg tracking-[0.2em]">•••• •••• •••• 4242</div>
        <div className="mt-2 flex justify-between text-xs text-leaf-100">
          <span>ARTAN SEPET TEST</span>
          <span>12/30</span>
        </div>
      </div>
      <p className="mt-2 text-xs text-muted">
        Bu bir demo uygulamadır: gerçek kart bilgisi istenmez ve para çekilmez.
      </p>

      {error && (
        <p className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}

      <div className="mt-6 flex gap-3">
        <button
          onClick={onClose}
          disabled={paying}
          className="flex-1 rounded-xl border border-line py-3 font-semibold transition hover:bg-cream disabled:opacity-50"
        >
          Vazgeç
        </button>
        <button
          onClick={onPay}
          disabled={paying}
          className="flex-[2] rounded-xl bg-leaf-600 py-3 font-semibold text-white shadow-lg shadow-leaf-600/20 transition hover:bg-leaf-700 disabled:opacity-70"
        >
          {paying ? "Ödeme alınıyor..." : `${tl(bag.currentPrice)} öde`}
        </button>
      </div>
    </Modal>
  );
}

function SuccessModal({ order, onClose }: { order: Order; onClose: () => void }) {
  return (
    <Modal onClose={onClose}>
      <div className="text-center">
        <div className="text-5xl">🎉</div>
        <h2 className="mt-3 text-2xl font-bold">Ödeme başarılı!</h2>
        <p className="mt-1 text-muted">
          {order.bagTitle} · {tl(order.price)}
        </p>
        <div className="mt-6 rounded-2xl bg-leaf-50 p-4">
          <div className="text-sm text-muted">Teslim kodun</div>
          <div className="mt-1 text-4xl font-extrabold tracking-[0.3em] text-leaf-700">{order.pickupCode}</div>
          <div className="mt-1 text-xs text-muted">Teslimde işletmeye söyle · {order.pickupEnd}&apos;a kadar</div>
        </div>
        <div className="mt-6 flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 rounded-xl border border-line py-3 font-semibold transition hover:bg-cream"
          >
            Kapat
          </button>
          <Link
            href="/siparislerim"
            className="flex-1 rounded-xl bg-leaf-600 py-3 font-semibold text-white transition hover:bg-leaf-700"
          >
            Siparişlerim
          </Link>
        </div>
      </div>
    </Modal>
  );
}

function BagCard({ bag: b, onOrder }: { bag: Bag; onOrder: () => void }) {
  const discount = Math.round((1 - b.currentPrice / b.originalPrice) * 100);
  const chip = "rounded-full bg-cream px-2.5 py-1 font-medium text-muted";
  return (
    <article className="flex flex-col overflow-hidden rounded-3xl border border-line bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg">
      <div className="relative bg-linear-to-br from-leaf-100 to-sun-100 p-5">
        <div className="text-4xl">{TYPE_ICON[b.businessType] ?? "🛍️"}</div>
        {discount > 0 && (
          <span className="absolute right-4 top-4 rounded-full bg-sun-500 px-3 py-1 text-sm font-bold text-white">
            -%{discount}
          </span>
        )}
        <h3 className="mt-3 text-lg font-bold">{b.title}</h3>
        <p className="text-sm text-muted">{b.businessName}</p>
      </div>

      <div className="flex flex-1 flex-col gap-4 p-5">
        <div className="flex flex-wrap gap-2 text-xs">
          {b.distanceKm !== null && <span className={chip}>📍 {b.distanceKm.toLocaleString("tr-TR")} km</span>}
          <span className={chip}>
            🕒 {b.pickupStart}–{b.pickupEnd}
          </span>
          {b.tags.map((t) => (
            <span key={t} className="rounded-full bg-leaf-50 px-2.5 py-1 font-medium text-leaf-700">
              {TAG_TR[t] ?? t}
            </span>
          ))}
        </div>

        <div>
          <div className="flex justify-between text-xs text-muted">
            <span>Kalan paket</span>
            <span>
              {b.quantity}/{b.initialQuantity}
            </span>
          </div>
          <div className="mt-1 h-2 rounded-full bg-leaf-50">
            <div
              className="h-2 rounded-full bg-leaf-500"
              style={{ width: `${(b.quantity / b.initialQuantity) * 100}%` }}
            />
          </div>
        </div>

        <div className="mt-auto flex items-end justify-between">
          <div>
            <div className="text-2xl font-extrabold text-leaf-700">{tl(b.currentPrice)}</div>
            {discount > 0 && <div className="text-sm text-muted line-through">{tl(b.originalPrice)}</div>}
          </div>
          <button
            onClick={onOrder}
            className="rounded-xl bg-leaf-600 px-4 py-2.5 font-semibold text-white transition hover:bg-leaf-700"
          >
            Sipariş ver
          </button>
        </div>
      </div>
    </article>
  );
}