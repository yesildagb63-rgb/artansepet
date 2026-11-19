"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api, getUser, saveSession, type User } from "@/lib/api";

type AuthResponse = { token: string; user: User };

const DEMO = [
  { label: "Ayşe (müşteri)", email: "ayse@ornek.com" },
  { label: "Moda Fırını (işletme)", email: "firin@artansepet.com" },
  { label: "Bahariye Pastanesi (işletme)", email: "pastane@artansepet.com" },
];

const SEMTLER = [
  { value: "Kadikoy", label: "Kadıköy" },
  { value: "Uskudar", label: "Üsküdar" },
  { value: "Besiktas", label: "Beşiktaş" },
  { value: "Taksim", label: "Taksim" },
];

export default function Home() {
  const router = useRouter();
  const [mode, setMode] = useState<"login" | "register">("login");
  const [form, setForm] = useState({ name: "", email: "", password: "", district: "Kadikoy" });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const go = (u: User) => router.push(u.role === "business" ? "/isletme" : "/paketler");

  // Zaten giris yapmissa dogrudan kendi sayfasina gonder
  useEffect(() => {
    const u = getUser();
    if (u) go(u);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);
    try {
      const res =
        mode === "login"
          ? await api<AuthResponse>("/login", {
              method: "POST",
              body: { email: form.email, password: form.password },
            })
          : await api<AuthResponse>("/register", { method: "POST", body: form });
      saveSession(res.token, res.user);
      go(res.user);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Bir hata oluştu");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="grid min-h-screen lg:grid-cols-2">
      {/* ---------- Sol: tanitim ---------- */}
      <section className="relative flex flex-col justify-between overflow-hidden bg-leaf-700 p-8 text-white lg:p-14">
        <div className="absolute -right-24 -top-24 size-80 rounded-full bg-leaf-500/40 blur-3xl" />
        <div className="absolute -bottom-32 -left-16 size-96 rounded-full bg-sun-500/25 blur-3xl" />

        <div className="relative flex items-center gap-2 text-lg font-bold">
          <span className="grid size-9 place-items-center rounded-xl bg-sun-400 text-leaf-900">AS</span>
          Artan Sepet
        </div>

        <div className="relative max-w-md py-12">
          <p className="mb-4 inline-block rounded-full bg-white/10 px-3 py-1 text-sm">
            Gün sonu, israf değil fırsat
          </p>
          <h1 className="text-4xl font-extrabold leading-tight lg:text-5xl">
            Lezzetli ürünler çöpe değil, <span className="text-sun-400">sepetine</span>.
          </h1>
          <p className="mt-5 text-lg text-leaf-100">
            Fırın, pastane ve restoranların gün sonu kalan ürünlerini sürpriz paketlerle uygun fiyata al.
          </p>
        </div>

        <div className="relative grid grid-cols-2 gap-2 text-center">
          {[
            ["%50'ye varan", "indirim"],
            
            ["6 haneli", "teslim kodu"],
          ].map(([value, label]) => (
            <div key={label} className="rounded-2xl bg-white/10 p-4 backdrop-blur">
              <div className="text-xl font-bold">{value}</div>
              <div className="text-sm text-leaf-100">{label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ---------- Sag: form ---------- */}
      <section className="flex items-center justify-center p-6 lg:p-14">
        <div className="w-full max-w-md">
          <div className="mb-6 flex rounded-xl bg-leaf-50 p-1">
            {(["login", "register"] as const).map((m) => (
              <button
                key={m}
                type="button"
                onClick={() => {
                  setMode(m);
                  setError(null);
                }}
                className={`flex-1 rounded-lg py-2 text-sm font-semibold transition ${
                  mode === m ? "bg-white text-leaf-700 shadow-sm" : "text-muted hover:text-ink"
                }`}
              >
                {m === "login" ? "Giriş yap" : "Kayıt ol"}
              </button>
            ))}
          </div>

          <h2 className="text-2xl font-bold">{mode === "login" ? "Tekrar hoş geldin" : "Hesap oluştur"}</h2>
          <p className="mt-1 text-muted">
            {mode === "login" ? "Sürpriz paketler seni bekliyor." : "Yakınındaki paketleri keşfetmeye başla."}
          </p>

          <form onSubmit={submit} className="mt-6 space-y-4">
            {mode === "register" && (
              <Field label="Ad soyad" value={form.name} onChange={(v) => setForm({ ...form, name: v })} />
            )}
            <Field
              label="E-posta"
              type="email"
              value={form.email}
              onChange={(v) => setForm({ ...form, email: v })}
            />
            <Field
              label="Şifre"
              type="password"
              value={form.password}
              onChange={(v) => setForm({ ...form, password: v })}
            />
            {mode === "register" && (
              <label className="block">
                <span className="text-sm font-medium">Semt</span>
                <select
                  value={form.district}
                  onChange={(e) => setForm({ ...form, district: e.target.value })}
                  className="mt-1 w-full rounded-xl border border-line bg-white px-4 py-3 outline-none focus:border-leaf-500 focus:ring-4 focus:ring-leaf-100"
                >
                  {SEMTLER.map((s) => (
                    <option key={s.value} value={s.value}>
                      {s.label}
                    </option>
                  ))}
                </select>
              </label>
            )}

            {error && (
              <p className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
            )}

            <button
              disabled={loading}
              className="w-full rounded-xl bg-leaf-600 py-3 font-semibold text-white shadow-lg shadow-leaf-600/20 transition hover:bg-leaf-700 disabled:opacity-60"
            >
              {loading ? "Bekleyin..." : mode === "login" ? "Giriş yap" : "Kayıt ol"}
            </button>
          </form>

          {mode === "login" && (
            <div className="mt-8 rounded-2xl border border-line bg-white p-4">
              <p className="text-sm font-semibold">
                Demo hesaplar <span className="font-normal text-muted">(şifre: 123456)</span>
              </p>
              <div className="mt-3 flex flex-wrap gap-2">
                {DEMO.map((d) => (
                  <button
                    key={d.email}
                    type="button"
                    onClick={() => setForm({ ...form, email: d.email, password: "123456" })}
                    className="rounded-full bg-sun-100 px-3 py-1.5 text-sm font-medium text-sun-700 transition hover:bg-sun-400 hover:text-leaf-900"
                  >
                    {d.label}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
      </section>
    </main>
  );
}

function Field({
  label,
  type = "text",
  value,
  onChange,
}: {
  label: string;
  type?: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <label className="block">
      <span className="text-sm font-medium">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required
        className="mt-1 w-full rounded-xl border border-line bg-white px-4 py-3 outline-none transition focus:border-leaf-500 focus:ring-4 focus:ring-leaf-100"
      />
    </label>
  );
}