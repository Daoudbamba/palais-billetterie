'use client'
import { useEffect, useState } from 'react'

export default function HomePage() {
  const [events, setEvents] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const base = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'
    fetch(`${base}/api/events`)
      .then(r => r.json())
      .then(setEvents)
      .catch(() => setEvents([]))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main style={{ padding: 24 }}>
      <h1>Billetterie – Événements</h1>
      {loading ? <p>Chargement…</p> : (
        <ul>
          {events.map(ev => (
            <li key={ev.id}>{ev.title} — Capacité: {ev.capacity}</li>
          ))}
        </ul>
      )}
    </main>
  )
}