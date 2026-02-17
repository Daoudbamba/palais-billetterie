"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { listRefunds, requestRefund, ApiError } from '../../lib/api';
import Link from 'next/link';

export default function RefundsPage() {
  const router = useRouter();
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);
  const [paymentId, setPaymentId] = useState('');
  const [amount, setAmount] = useState('');
  const [busy, setBusy] = useState(false);

  function handleApiError(e: any, prefix = 'Erreur') {
    if (e instanceof ApiError && e.status === 401) {
      setMessage('Session expirée ou non authentifiée. Merci de vous reconnecter.');
      router.replace('/auth/login');
      return;
    }
    setMessage(`${prefix}: ${e?.message ?? 'Erreur inconnue'}`);
  }

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          router.replace('/auth/login');
          return;
        }
        const data = await listRefunds(token);
        setItems(data);
      } catch (e: any) {
        handleApiError(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [router]);

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
      handleApiError(e, 'Erreur remboursement');
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
      <p style={{ maxWidth: 600, fontSize: 14, color: '#555' }}>
        Règles actuelles : un seul remboursement possible par paiement et uniquement
        sur le montant total payé (pas de remboursement partiel).
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <div>
                  <strong>Refund</strong> {r.id}
                  {r.status && (
                    <span
                      style={{
                        marginLeft: 8,
                        padding: '2px 8px',
                        borderRadius: 999,
                        fontSize: 12,
                        background:
                          r.status === 'SUCCESS'
                            ? '#e6ffed'
                            : r.status === 'PENDING'
                            ? '#fff7e6'
                            : '#fde2e1',
                        border: '1px solid #ddd',
                      }}
                    >
                      {r.status}
                    </span>
                  )}
                </div>
                {r.createdAt && (
                  <span style={{ fontSize: 12, color: '#666' }}>
                    Créé le {new Date(r.createdAt).toLocaleString('fr-FR')}
                  </span>
                )}
              </div>
              <div style={{ marginTop: 4 }}>Montant remboursé: {r.amount} €</div>
              {r.payment?.id && (
                <div style={{ marginTop: 2 }}>Paiement: {r.payment.id}</div>
              )}
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
