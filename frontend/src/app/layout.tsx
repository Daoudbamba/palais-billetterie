export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <body style={{ fontFamily: 'system-ui, sans-serif', margin: 0 }}>
        <header style={{ padding: '12px 24px', borderBottom: '1px solid #eee', display: 'flex', gap: 16 }}>
          <a href="/">Accueil</a>
          <a href="/events">Événements</a>
          <a href="/payments">Paiements</a>
          <a href="/tickets">Billets</a>
          <a href="/refunds">Remboursements</a>
          <span style={{ marginLeft: 'auto' }}>
            <a href="/auth/login">Connexion</a>
            {' '}·{' '}
            <a href="/auth/register">Inscription</a>
          </span>
        </header>
        {children}
      </body>
    </html>
  );
}