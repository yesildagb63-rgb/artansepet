// Java API ile konusan tek yer. Sayfalar fetch'i dogrudan cagirmaz, bu dosyayi kullanir.

export type User = {
  id: string;
  name: string;
  email: string;
  role: "customer" | "business";
  followerCount: number | null;
};

export type Bag = {
  id: string;
  title: string;
  businessId: string;
  businessName: string;
  businessType: string;
  address: string;
  currentPrice: number;
  originalPrice: number;
  quantity: number;
  initialQuantity: number;
  pickupStart: string;
  pickupEnd: string;
  tags: string[];
  distanceKm: number | null;
  expired: boolean;
};

export type Order = {
  id: string;
  bagTitle: string;
  businessName: string;
  customerName: string;
  price: number;
  state: string;
  open: boolean;
  pickupCode: string | null;
  createdAt: string;
  pickupEnd: string;
};

export type Business = {
  id: string;
  name: string;
  type: string;
  address: string;
  followed: boolean;
  followerCount: number;
};

const TOKEN_KEY = "artansepet_token";
const USER_KEY = "artansepet_user";

export function saveSession(token: string, user: User) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function getUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

/** API'ye istek atar. Hata olursa Java'nin gonderdigi mesajla Error firlatir. */
export async function api<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const token = localStorage.getItem(TOKEN_KEY);
  const res = await fetch(`/api${path}`, {
    method: options.method ?? "GET",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    cache: "no-store",
  });
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401) {
      clearSession(); // sunucu yeniden baslarsa oturum duser
    }
    throw new Error(data?.error ?? "Beklenmeyen bir hata oluştu");
  }
  return data as T;
}

/** Turk lirasi bicimi: 75 -> "75,00 ₺" */
export const tl = (n: number) =>
  n.toLocaleString("tr-TR", { style: "currency", currency: "TRY" });