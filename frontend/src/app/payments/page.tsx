"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { listPayments, ApiError } from '../../lib/api';
import Link from 'next/link';

export default function PaymentsPage() {
  const router = useRouter();
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  function handleApiError(e: any) {
    if (e instanceof ApiError && e.status === 401) {
      setMessage('Session expirée ou non authentifiée. Merci de vous reconnecter.');
      router.replace('/auth/login');
      return;
    }
    setMessage(`Erreur: ${e?.message ?? 'Erreur inconnue'}`);
  }

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          router.replace('/auth/login');
          return;
        }
        const data = await listPayments(token);
        setItems(data);
      } catch (e: any) {
        handleApiError(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [router]);

  return (
    <main style={{ padding: 24 }}>
      <h1>Paiements</h1>
      <p>
        <Link href="/auth/login">Connexion</Link> · <Link href="/auth/register">Inscription</Link>
      </p>
      {message && <p>{message}</p>}
      {loading ? <p>Chargement…</p> : (
        <ul style={{ display: 'grid', gap: 12 }}>
          {items.map(p => (
            <li key={p.id} style={{ border: '1px solid #ddd', padding: 12 }}>
              <div><strong>Payment</strong> {p.id}</div>
              <div>Statut: {p.status}</div>
              <div>Montant: {p.amount}</div>
              {p.order?.id && <div>Commande: {p.order.id}</div>}
              <div style={{ marginTop: 8 }}>
                <Link href={`/payments/${p.id}`}>Détails</Link>
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
