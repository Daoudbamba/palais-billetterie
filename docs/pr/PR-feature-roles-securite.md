# PR: Sécurité – Rôles par endpoint

## Objectif
Rendre l’API plus sûre en imposant des rôles sur les endpoints sensibles.

## Changements clés
- `POST /api/orders/**`: nécessite rôle `USER`.
- `GET /api/orders/**`: nécessite authentification.
- `/api/payments/**`: protégés (hors `POST /api/payments/stripe/webhook`).

## Tests / Vérifications
- `POST /api/orders` sans token → refus (403).
- Enregistrement + login → token; `POST /api/orders` avec Bearer → 200 JSON.

## Risques et rollback
- Risque faible; rollback simple via revert de la PR.
