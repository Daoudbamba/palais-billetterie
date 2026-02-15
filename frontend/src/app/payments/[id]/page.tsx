"use client";

import { useEffect, useState } from 'react';
import { getPayment } from '../../../lib/api';
import Link from 'next/link';

export default function PaymentDetailPage({ params }: { params: { id: string } }) {
  const { id } = params;
  const [payment, setPayment] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const token = localStorage.getItem('accessToken');
        if (!token) {
          setMessage('Veuillez vous connecter pour voir le paiement.');
          setPayment(null);
          return;
        }
        const data = await getPayment(token, id);
        setPayment(data);
      } catch (e: any) {
        setMessage(`Erreur: ${e.message}`);
      } finally {
        setLoading(false);
      }
    })();
  }, [id]);

  return (
    <main style={{ padding: 24 }}>
      <h1>Paiement {id}</h1>
      <p>
        <Link href="/payments">← Retour aux paiements</Link>
      </p>
      {message && <p>{message}</p>}
      {loading ? <p>Chargement…</p> : payment ? (
        <div style={{ border: '1px solid #ddd', padding: 12 }}>
          <div>Statut: {payment.status}</div>
          <div>Montant: {payment.amount}</div>
          <div>Provider: {payment.provider}</div>
          <div>ProviderPaymentId: {payment.providerPaymentId}</div>
          {payment.order && (
            <div style={{ marginTop: 8 }}>
              <div>Commande: {payment.order.id}</div>
            </div>
          )}
        </div>
      ) : (
        <p>Paiement introuvable.</p>
      )}
    </main>
  );
}
