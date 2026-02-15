"use client";

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getTicket } from '../../../lib/api';
import Link from 'next/link';

export default function TicketDetailPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const router = useRouter();
  const [ticket, setTicket] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          router.replace('/auth/login');
          return;
        }
        const data = await getTicket(token, id);
        setTicket(data);
      } catch (e: any) {
        setMessage(`Erreur: ${e.message}`);
      } finally {
        setLoading(false);
      }
    })();
  }, [id, router]);

  return (
    <main style={{ padding: 24 }}>
      <h1>Billet {id}</h1>
      <p>
        <Link href="/tickets">← Retour aux billets</Link>
      </p>
      {message && <p>{message}</p>}
      {loading ? <p>Chargement…</p> : ticket ? (
        <div style={{ border: '1px solid #ddd', padding: 12 }}>
          <div>Status: {ticket.status}</div>
          {ticket.event && (
            <div style={{ marginTop: 8 }}>
              <div>Événement: {ticket.event.title ?? ticket.event.id}</div>
            </div>
          )}
          {ticket.order && (
            <div style={{ marginTop: 8 }}>
              <div>Commande: {ticket.order.id}</div>
            </div>
          )}
        </div>
      ) : (
        <p>Billet introuvable.</p>
      )}
    </main>
  );
}
