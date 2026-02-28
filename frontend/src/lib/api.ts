export const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? 'http://127.0.0.1:8080';

// Erreur d'API avec code HTTP (utile pour gérer les 401 côté UI)
export class ApiError extends Error {
  status?: number;

  constructor(message: string, status?: number) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

function assertOk(res: Response, baseMessage: string) {
  if (!res.ok) {
    throw new ApiError(`${baseMessage} (${res.status})`, res.status);
  }
}

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
  assertOk(res, 'Erreur chargement événements');
  return res.json();
}

export async function register(name: string, email: string, password: string) {
  const res = await fetch(`${API_BASE}/api/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password })
  });
  if (!res.ok && res.status !== 409) throw new ApiError('Inscription échouée', res.status);
  return res.json();
}

export async function login(email: string, password: string) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  assertOk(res, 'Connexion échouée');
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
  assertOk(res, 'Création commande échouée');
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
  assertOk(res, 'CreateIntent échouée');
  return res.json() as Promise<{ clientSecret: string; paymentId: string }>;
}

export async function listPayments(token: string) {
  const res = await fetch(`${API_BASE}/api/payments`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, 'Liste paiements échouée');
  return res.json();
}

export async function getPayment(token: string, id: string) {
  const res = await fetch(`${API_BASE}/api/payments/${id}`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, `Paiement ${id} introuvable`);
  return res.json();
}

// Tickets
export async function listTickets(token: string) {
  const res = await fetch(`${API_BASE}/api/tickets`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, 'Liste tickets échouée');
  return res.json();
}

export async function getTicket(token: string, id: string) {
  const res = await fetch(`${API_BASE}/api/tickets/${id}`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, `Ticket ${id} introuvable`);
  return res.json();
}

// Refunds
export async function listRefunds(token: string) {
  const res = await fetch(`${API_BASE}/api/refunds`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, 'Liste remboursements échouée');
  return res.json();
}

export async function getRefund(token: string, id: string) {
  const res = await fetch(`${API_BASE}/api/refunds/${id}`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  assertOk(res, `Remboursement ${id} introuvable`);
  return res.json();
}

export async function requestRefund(token: string, paymentId: string, amount: number) {
  const res = await fetch(`${API_BASE}/api/refunds`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ paymentId, amount })
  });
  assertOk(res, 'Demande remboursement échouée');
  return res.json();
}

// Dev only: simulate a Stripe webhook success (local testing)
export async function simulateStripeSuccess(token: string, paymentId: string) {
  const url = new URL(`${API_BASE}/api/payments/stripe/dev/simulate-success`);
  url.searchParams.set('paymentId', paymentId);
  const res = await fetch(url.toString(), {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  assertOk(res, 'Simulation webhook échouée');
  return res.text();
}
