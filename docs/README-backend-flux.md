# Flux complet backend — Billetterie

Ce document décrit un parcours type: inscription, promotion de rôle, création d’événement, commande, paiement Stripe, génération de ticket, scan, remboursement.

## Pré-requis
- Backend démarré sur `http://localhost:8080`
- Base PostgreSQL opérationnelle (docker compose)
- Variables Stripe et SMTP configurées dans `backend/src/main/resources/application.yml` (mode dev possible avec clés placeholder)

## 1) Auth — inscrire et se connecter
```bash
# Inscription (retourne access/refresh)
curl -sS -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Contrôleur","email":"controller@demo.com","password":"Passw0rd!"}'

# Connexion
auth=$(curl -sS -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"controller@demo.com","password":"Passw0rd!"}')
ACCESS=$(echo "$auth" | jq -r .access)
REFRESH=$(echo "$auth" | jq -r .refresh)
```

## 2) Promotion de rôle (ADMIN nécessaire)
```bash
# Lister utilisateurs (ADMIN)
curl -sS -H "Authorization: Bearer $ACCESS" http://localhost:8080/api/users
# Mettre le rôle CONTROLLER
USER_ID=<id_utilisateur>
curl -sS -X PUT http://localhost:8080/api/users/$USER_ID \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"name":"Contrôleur","email":"controller@demo.com","phone":null,"role":"CONTROLLER"}'
```

## 3) Événement (PROMOTER|ADMIN)
```bash
EVENT=$(curl -sS -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"title":"Concert","description":"Super concert","startDateTime":"2026-03-01T20:00:00Z","endDateTime":"2026-03-01T23:00:00Z","venue":"Palais","capacity":100}')
EVENT_ID=$(echo "$EVENT" | jq -r .id)
```

## 4) Commande
```bash
ORDER=$(curl -sS -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"userId":"'$USER_ID'","eventId":"'$EVENT_ID'","amount":49.99}')
ORDER_ID=$(echo "$ORDER" | jq -r .id)

# Lecture: ADMIN voit tout; USER voit uniquement ses commandes
curl -sS -H "Authorization: Bearer $ACCESS" http://localhost:8080/api/orders/$ORDER_ID
```

## 5) Paiement Stripe
```bash
# Créer un PaymentIntent (USER|ADMIN)
curl -sS -X POST "http://localhost:8080/api/payments/stripe/create-intent?orderId=$ORDER_ID" \
  -H "Authorization: Bearer $ACCESS"
# Webhook Stripe côté backend: /api/payments/stripe/webhook (configuré chez Stripe)
```

## 6) Ticket (créé au succès du paiement via webhook)
```bash
# Lecture tickets (ADMIN|PROMOTER)
curl -sS -H "Authorization: Bearer $ACCESS" http://localhost:8080/api/tickets
# Récupérer code pour scan
TICKET_CODE=<code_ticket>
```

## 7) Scan (CONTROLLER)
```bash
# Vérifier
curl -sS -X POST http://localhost:8080/api/scan/verify \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"code":"'$TICKET_CODE'"}'
# Utiliser
curl -sS -X POST http://localhost:8080/api/scan/use \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"code":"'$TICKET_CODE'"}'
```

## 8) Remboursement
```bash
# Demande (USER|ADMIN)
curl -sS -X POST http://localhost:8080/api/refunds \
  -H "Authorization: Bearer $ACCESS" -H "Content-Type: application/json" \
  -d '{"paymentId":"'$PAYMENT_ID'","amount":1000}'
# Passage à success/failed (ADMIN)
REFUND_ID=<id_refund>
curl -sS -X POST "http://localhost:8080/api/refunds/$REFUND_ID/success?providerRefundId=re_demo_123" \
  -H "Authorization: Bearer $ACCESS"
```

## Notes
- Les droits sont gérés dans `SecurityConfig`.
- Un `USER` peut lire uniquement ses commandes; `ADMIN` voit tout.
- CORS est activé pour `http://localhost:3000`.
