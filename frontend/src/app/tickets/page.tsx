"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { listTickets, ApiError } from '../../lib/api';
import Link from 'next/link';

export default function TicketsPage() {
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
        const data = await listTickets(token);
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
      <h1>Billets</h1>
      <p>
        <Link href="/auth/login">Connexion</Link> · <Link href="/auth/register">Inscription</Link>
      </p>
      {message && <p>{message}</p>}
      {loading ? <p>Chargement…</p> : (
        <ul style={{ display: 'grid', gap: 12 }}>
          {items.map(t => (
            <li key={t.id} style={{ border: '1px solid #ddd', padding: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline' }}>
                <div>
                  <strong>Ticket</strong> {t.code ?? t.id}
                  {t.status && (
                    <span style={{
                      marginLeft: 8,
                      padding: '2px 8px',
                      borderRadius: 999,
                      fontSize: 12,
                      background: t.status === 'VALID' ? '#e6ffed' : t.status === 'USED' ? '#e5e7eb' : '#fff7e6',
                      border: '1px solid #ddd'
                    }}>
                      {t.status}
                    </span>
                  )}
                </div>
                {t.createdAt && (
                  <span style={{ fontSize: 12, color: '#666' }}>
                    Créé le {new Date(t.createdAt).toLocaleString('fr-FR')}
                  </span>
                )}
              </div>
              {t.event && (
                <div style={{ marginTop: 4 }}>
                  <div>Événement: {t.event.title ?? t.event.id}</div>
                  {t.event.venue && <div>Lieu: {t.event.venue}</div>}
                  {t.event.startDateTime && (
                    <div>Début: {new Date(t.event.startDateTime).toLocaleString('fr-FR')}</div>
                  )}
                </div>
              )}
              {t.order?.id && <div style={{ marginTop: 4 }}>Commande: {t.order.id}</div>}
              <div style={{ marginTop: 8 }}>
                <Link href={`/tickets/${t.id}`}>Détails</Link>
              </div>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
