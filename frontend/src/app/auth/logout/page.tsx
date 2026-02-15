"use client";

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function LogoutPage() {
  const router = useRouter();
  useEffect(() => {
    try {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    } catch {}
    router.replace('/auth/login');
  }, [router]);

  return (
    <main style={{ padding: 24 }}>
      <p>Déconnexion…</p>
    </main>
  );
}
