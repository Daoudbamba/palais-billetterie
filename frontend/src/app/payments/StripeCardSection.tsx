"use client";

import { useState } from "react";
import { loadStripe } from "@stripe/stripe-js";
import {
  Elements,
  CardElement,
  useStripe,
  useElements,
} from "@stripe/react-stripe-js";

// Clé publique Stripe (test ou live) injectée via NEXT_PUBLIC_STRIPE_PK
const stripePk = process.env.NEXT_PUBLIC_STRIPE_PK;
// Ne pas appeler loadStripe avec une chaîne vide pour éviter l'IntegrationError
const stripePromise = stripePk ? loadStripe(stripePk) : null;

function CardForm({ clientSecret }: { clientSecret: string }) {
  const stripe = useStripe();
  const elements = useElements();
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setMessage(null);
    if (!stripe || !elements) {
      setMessage("Stripe non initialisé.");
      return;
    }
    const card = elements.getElement(CardElement);
    if (!card) {
      setMessage("Champ carte introuvable.");
      return;
    }
    setSubmitting(true);
    try {
      const result = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card,
        },
      });
      if (result.error) {
        setMessage(result.error.message ?? "Erreur de paiement");
      } else if (result.paymentIntent) {
        setMessage(`Paiement Stripe: ${result.paymentIntent.status}`);
      }
    } catch (err: any) {
      setMessage(err.message ?? "Erreur inattendue");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ marginTop: 12 }}>
      <div
        style={{
          border: "1px solid #ddd",
          borderRadius: 4,
          padding: 12,
          background: "#fafafa",
        }}
      >
        <p style={{ marginTop: 0, marginBottom: 8 }}>Paiement réel Stripe (mode test)</p>
        <div
          style={{
            padding: 8,
            border: "1px solid #ccc",
            borderRadius: 4,
            background: "#fff",
          }}
        >
          <CardElement options={{ hidePostalCode: true }} />
        </div>
        <button
          type="submit"
          disabled={submitting || !stripe || !elements}
          style={{ marginTop: 12 }}
        >
          {submitting ? "Traitement..." : "Payer avec Stripe"}
        </button>
        {message && (
          <div style={{ marginTop: 8, fontSize: 14 }}>{message}</div>
        )}
      </div>
    </form>
  );
}

export function StripeCardSection({ clientSecret }: { clientSecret: string }) {
  if (!clientSecret) return null;

  // Si la clé publique n'est pas définie, on affiche une note de config.
  if (!stripePk || !stripePromise) {
    return (
      <p style={{ marginTop: 8, fontSize: 14 }}>
        Pour activer le paiement réel Stripe, définissez
        {" "}
        <code>NEXT_PUBLIC_STRIPE_PK</code>
        {" "}
        dans <code>.env.local</code> (clé publique Stripe, mode test ou live).
      </p>
    );
  }

  return (
    <Elements stripe={stripePromise}>
      <CardForm clientSecret={clientSecret} />
    </Elements>
  );
}
