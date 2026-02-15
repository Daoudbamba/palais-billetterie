export const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? 'http://127.0.0.1:8080';

export type EventItem = {
  id: string;
  title: string;
  description?: string;
  start_date_time?: string;
  end_date_time?: string;
  venue?: string;
  capacity?: number;
};

export async function getHealth(): Promise<boolean> {
  const res = await fetch(`${API_BASE}/api/health`, { cache: 'no-store' });
  return res.ok;
}

export async function getEvents(): Promise<EventItem[]> {
  const res = await fetch(`${API_BASE}/api/events`, { cache: 'no-store' });
  if (!res.ok) throw new Error('Erreur chargement événements');
  return res.json();
}

export async function register(name: string, email: string, password: string) {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password })
  });
  if (!res.ok && res.status !== 409) throw new Error('Inscription échouée');
  return res.json();
}

export async function login(email: string, password: string) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!res.ok) throw new Error('Connexion échouée');
  return res.json() as Promise<{ accessToken: string; refreshToken: string }>;
}

export async function createOrder(token: string, payload: Record<string, unknown>) {
  const res = await fetch(`${API_BASE}/api/orders`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(payload)
  });
  if (!res.ok) throw new Error(`Création commande échouée (${res.status})`);
  return res.json();
}

export async function createStripeIntent(token: string, orderId: string) {
  const url = new URL(`${API_BASE}/api/payments/stripe/create-intent`);
  url.searchParams.set('orderId', orderId);
  const res = await fetch(url.toString(), {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  if (!res.ok) throw new Error(`CreateIntent échouée (${res.status})`);
  return res.json() as Promise<{ clientSecret: string; paymentId: string }>;
}
