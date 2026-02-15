"use client";

import { useState } from 'react';
import { login } from '../../../lib/api';
import Link from 'next/link';

export default function LoginPage() {
  const [email, setEmail] = useState('test.user@example.com');
  const [password, setPassword] = useState('P@ssw0rd!');
  const [error, setError] = useState<string | null>(null);
  const [token, setToken] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const resp = await login(email, password);
      setToken(resp.accessToken);
      localStorage.setItem('accessToken', resp.accessToken);
    } catch (err: any) {
      setError(err.message ?? 'Erreur de connexion');
    }
  }

  return (
    <div style={{ maxWidth: 420, margin: '40px auto', padding: 16 }}>
      <h1>Connexion</h1>
      <form onSubmit={onSubmit} style={{ display: 'grid', gap: 12 }}>
        <input type="email" value={email} onChange={e => setEmail(e.target.value)} placeholder="Email" required />
        <input type="password" value={password} onChange={e => setPassword(e.target.value)} placeholder="Mot de passe" required />
        <button type="submit">Se connecter</button>
      </form>
      {error && <p style={{ color: 'crimson' }}>{error}</p>}
      {token && <p>Token stocké. <Link href="/events">Voir les événements</Link></p>}
      <p style={{ marginTop: 12 }}>
        Pas de compte ? <Link href="/auth/register">Inscription</Link>
      </p>
    </div>
  );
}
