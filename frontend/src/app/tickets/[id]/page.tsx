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
          <div style={{ marginBottom: 8 }}>
            <div><strong>Code billet:</strong> {ticket.code ?? ticket.id}</div>
            {ticket.status && <div><strong>Status:</strong> {ticket.status}</div>}
            {ticket.createdAt && (
              <div><strong>Créé le:</strong> {new Date(ticket.createdAt).toLocaleString('fr-FR')}</div>
            )}
          </div>
          {ticket.event && (
            <div style={{ marginTop: 8 }}>
              <h2 style={{ margin: '8px 0 4px' }}>Événement</h2>
              <div><strong>{ticket.event.title ?? ticket.event.id}</strong></div>
              {ticket.event.venue && <div>Lieu: {ticket.event.venue}</div>}
              {ticket.event.startDateTime && (
                <div>Début: {new Date(ticket.event.startDateTime).toLocaleString('fr-FR')}</div>
              )}
            </div>
          )}
          {ticket.order && (
            <div style={{ marginTop: 8 }}>
              <h2 style={{ margin: '8px 0 4px' }}>Commande</h2>
              <div>Id commande: {ticket.order.id}</div>
            </div>
          )}
        </div>
      ) : (
        <p>Billet introuvable.</p>
      )}
    </main>
  );
}
