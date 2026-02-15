"use client";

import { useEffect, useState } from 'react';
import { getEvents, createOrder, createStripeIntent } from '../../lib/api';
import Link from 'next/link';

export default function EventsPage() {
  const [events, setEvents] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [orderId, setOrderId] = useState('c1d2e3f4-1111-2222-3333-444455556666');
  const [intent, setIntent] = useState<{ clientSecret: string; paymentId: string } | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const data = await getEvents();
        setEvents(data);
      } catch {
        setEvents([]);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function order(evId: string) {
    setMessage(null);
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setMessage('Veuillez vous connecter avant de commander.');
        return;
      }
      const resp = await createOrder(token, { eventId: evId, quantity: 1 });
      if (resp?.orderId) {
        setOrderId(String(resp.orderId));
      }
      setMessage(`Commande créée: ${JSON.stringify(resp)}`);
    } catch (e: any) {
      setMessage(`Erreur commande: ${e.message}`);
    }
  }

  async function createIntent() {
    setMessage(null);
    setIntent(null);
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setMessage('Veuillez vous connecter avant de créer un paiement.');
        return;
      }
      const resp = await createStripeIntent(token, orderId);
      setIntent(resp);
      setMessage(`Intent créé. paymentId=${resp.paymentId}`);
    } catch (e: any) {
      setMessage(`Erreur intent: ${e.message}`);
    }
  }

  return (
    <main style={{ padding: 24 }}>
      <h1>Événements</h1>
      <p>
        <Link href="/auth/login">Connexion</Link> · <Link href="/auth/register">Inscription</Link>
      </p>
      {loading ? <p>Chargement…</p> : (
        <ul style={{ display: 'grid', gap: 12 }}>
          {events.map(ev => (
            <li key={ev.id} style={{ border: '1px solid #ddd', padding: 12 }}>
              <strong>{ev.title}</strong>
              <div>Capacité: {ev.capacity}</div>
              <button onClick={() => order(ev.id)} style={{ marginTop: 8 }}>Commander</button>
            </li>
          ))}
        </ul>
      )}
      <div style={{ marginTop: 24, paddingTop: 12, borderTop: '1px solid #ddd' }}>
        <h2>Paiement (mode dev)</h2>
        <label>
          Order UUID:&nbsp;
          <input value={orderId} onChange={e => setOrderId(e.target.value)} style={{ width: 380 }} />
        </label>
        <div style={{ marginTop: 8 }}>
          <button onClick={createIntent}>Créer PaymentIntent (fake)</button>
        </div>
        {intent && (
          <div style={{ marginTop: 8 }}>
            <div>clientSecret: {intent.clientSecret}</div>
            <div>paymentId: {intent.paymentId}</div>
          </div>
        )}
      </div>
      {message && <p style={{ marginTop: 12 }}>{message}</p>}
    </main>
  );
}
