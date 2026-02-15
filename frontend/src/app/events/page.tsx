"use client";

import { useEffect, useState, useRef } from 'react';
import { getEvents, createOrder, createStripeIntent, getPayment } from '../../lib/api';
import Link from 'next/link';

export default function EventsPage() {
  const [events, setEvents] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [orderId, setOrderId] = useState('c1d2e3f4-1111-2222-3333-444455556666');
  const [intent, setIntent] = useState<{ clientSecret: string; paymentId: string } | null>(null);
  const [busy, setBusy] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState<string | null>(null);
  const pollRef = useRef<any>(null);

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
    setBusy(true);
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setMessage('Veuillez vous connecter avant de créer un paiement.');
        return;
      }
      const resp = await createStripeIntent(token, orderId);
      setIntent(resp);
      setMessage(`Intent créé. paymentId=${resp.paymentId}`);
      startPolling(resp.paymentId);
    } catch (e: any) {
      setMessage(`Erreur intent: ${e.message}`);
    } finally {
      setBusy(false);
    }
  }

  async function refreshPaymentStatus() {
    setPaymentStatus(null);
    try {
      const token = localStorage.getItem('accessToken');
      if (!token || !intent?.paymentId) {
        setMessage('Token manquant ou paymentId indisponible.');
        return;
      }
      const p = await getPayment(token, String(intent.paymentId));
      setPaymentStatus(p.status ?? 'INCONNU');
    } catch (e: any) {
      setMessage(`Erreur statut paiement: ${e.message}`);
    }
  }

  function startPolling(paymentId: string) {
    stopPolling();
    const token = localStorage.getItem('accessToken');
    if (!token || !paymentId) return;
    pollRef.current = setInterval(async () => {
      try {
        const p = await getPayment(token, String(paymentId));
        const st = p.status ?? 'INCONNU';
        setPaymentStatus(st);
        if (st === 'SUCCESS' || st === 'FAILED') {
          stopPolling();
          setMessage(st === 'SUCCESS' ? 'Paiement confirmé.' : 'Paiement échoué.');
        }
      } catch (e: any) {
        stopPolling();
        setMessage(`Polling interrompu: ${e.message}`);
      }
    }, 3000);
  }

  function stopPolling() {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }

  useEffect(() => {
    return () => {
      stopPolling();
    };
  }, []);

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
          <button onClick={createIntent} disabled={busy}>Créer PaymentIntent (fake)</button>
        </div>
        {intent && (
          <div style={{ marginTop: 8 }}>
            <div>clientSecret: {intent.clientSecret}</div>
            <div>paymentId: {intent.paymentId}</div>
            <div style={{ marginTop: 8 }}>
              <button onClick={refreshPaymentStatus}>Actualiser statut paiement</button>
              {paymentStatus && <span style={{ marginLeft: 8 }}>Statut: {paymentStatus}</span>}
            </div>
          </div>
        )}
      </div>
      {message && (
        <div style={{ position: 'fixed', right: 16, bottom: 16, background: '#222', color: '#fff', padding: '8px 12px', borderRadius: 6 }}>
          {message}
        </div>
      )}
    </main>
  );
}
