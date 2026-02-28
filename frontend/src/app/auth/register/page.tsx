"use client";

import { useState } from 'react';
import { register } from '../../../lib/api';
import Link from 'next/link';

export default function RegisterPage() {
  const [name, setName] = useState('Test User');
  const [email, setEmail] = useState('test.user@example.com');
  const [password, setPassword] = useState('P@ssw0rd!');
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      const resp = await register(name, email, password);
      setMessage('Inscription réussie (ou déjà inscrite). Vous pouvez vous connecter.');
    } catch (err: any) {
      setError(err.message ?? 'Erreur d\'inscription');
    }
  }

  return (
    <div style={{ maxWidth: 420, margin: '40px auto', padding: 16 }}>
      <h1>Inscription</h1>
      <form onSubmit={onSubmit} style={{ display: 'grid', gap: 12 }}>
        <input value={name} onChange={e => setName(e.target.value)} placeholder="Nom" required />
        <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required />
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Mot de passe" required />
        <button type="submit">Créer un compte</button>
      </form>
      {message && <p style={{ color: 'green' }}>{message}</p>}
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      <p style={{ marginTop: 12 }}>
        Déjà inscrit ? <Link href="/auth/login">Connexion</Link>
      </p>
    </div>
  );
}
