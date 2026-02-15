"use client";

import { useEffect, useState } from 'react';
import { listTickets } from '../../lib/api';
import Link from 'next/link';

export default function TicketsPage() {
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          setMessage('Veuillez vous connecter pour voir vos billets.');
          setItems([]);
          return;
        }
        const data = await listTickets(token);
        setItems(data);
      } catch (e: any) {
        setMessage(`Erreur: ${e.message}`);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

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
              <div><strong>Ticket</strong> {t.id}</div>
              <div>Status: {t.status}</div>
              {t.event?.title && <div>Événement: {t.event.title}</div>}
              {t.order?.id && <div>Commande: {t.order.id}</div>}
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
