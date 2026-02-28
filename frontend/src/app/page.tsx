'use client'
import { useEffect, useState } from 'react'
import { getEvents } from '../lib/api'

export default function HomePage() {
  const [events, setEvents] = useState<any[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    (async () => {
      try {
        const data = await getEvents();
        setEvents(data);
      } catch {
        setEvents([]);
      } finally {
        setLoading(false);
      }
    })();
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