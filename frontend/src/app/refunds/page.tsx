"use client";

import { useEffect, useState } from 'react';
import { listRefunds, requestRefund } from '../../lib/api';
import Link from 'next/link';

export default function RefundsPage() {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [paymentId, setPaymentId] = useState('');
  const [amount, setAmount] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          setMessage('Veuillez vous connecter pour voir vos remboursements.');
          setItems([]);
          return;
        }
        const data = await listRefunds(token);
        setItems(data);
      } catch (e: any) {
        setMessage(`Erreur: ${e.message}`);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  async function submitRefund() {
    setBusy(true);
    setMessage(null);
    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setMessage('Veuillez vous connecter.');
        return;
      }
      if (!paymentId || !amount) {
        setMessage('PaymentId et montant requis.');
        return;
      }
      const amt = parseFloat(amount);
      const r = await requestRefund(token, paymentId, amt);
      setMessage(`Remboursement demandé: ${r.id}`);
    } catch (e: any) {
      setMessage(`Erreur remboursement: ${e.message}`);
    } finally {
      setBusy(false);
    }
  }

  return (
    <main style={{ padding: 24 }}>
      <h1>Remboursements</h1>
      <p>
        <Link href="/auth/login">Connexion</Link> · <Link href="/auth/register">Inscription</Link>
      </p>
      {message && <p>{message}</p>}
      <div style={{ border: '1px solid #ddd', padding: 12, marginBottom: 16 }}>
        <h2>Demande de remboursement</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <input placeholder="Payment UUID" value={paymentId} onChange={e => setPaymentId(e.target.value)} style={{ width: 300 }} />
          <input placeholder="Montant" value={amount} onChange={e => setAmount(e.target.value)} style={{ width: 120 }} />
          <button onClick={submitRefund} disabled={busy}>Demander</button>
        </div>
      </div>
      {loading ? <p>Chargement…</p> : (
        <ul style={{ display: 'grid', gap: 12 }}>
          {items.map(r => (
            <li key={r.id} style={{ border: '1px solid #ddd', padding: 12 }}>
              <div><strong>Refund</strong> {r.id}</div>
              <div>Statut: {r.status}</div>
              <div>Montant: {r.amount}</div>
              {r.payment?.id && <div>Paiement: {r.payment.id}</div>}
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
