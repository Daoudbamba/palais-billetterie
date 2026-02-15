# PR: Sécurité – Rôles par endpoint

## Objectif
Rendre l’API plus sûre en imposant des rôles sur les endpoints sensibles.

## Changements clés
- `POST /api/orders/**`: nécessite rôle `USER`.
- `GET /api/orders/**`: nécessite authentification.
- `/api/payments/**`: protégés (hors `POST /api/payments/stripe/webhook`).
 - `GET /api/health`, `GET /api/events` (et détail) restent publics.
 - Sessions stateless; CORS permissif en dev.

## Tests / Vérifications
- `POST /api/orders` sans token → refus (403).
- Enregistrement + login → token; `POST /api/orders` avec Bearer → 200 JSON.
 - `GET /api/events` reste 200 sans token.
 - Webhook Stripe (`POST /api/payments/stripe/webhook`) accessible sans token en dev.

## Risques et rollback
- Risque faible; rollback simple via revert de la PR.

## Base de PR
- `develop`
