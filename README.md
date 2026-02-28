# Palais de la Culture – Billetterie (Monorepo)

Backend (Spring Boot), Frontend (Next.js) et Infra (Docker Compose) pour la billetterie digitale.

## Structure
- backend/ — API REST Spring Boot (Java 21)
- frontend/ — Application Next.js (React + TypeScript)
- infra/ — Docker Compose (PostgreSQL)
- docs/ — Spécifications et diagrammes

## Prérequis
- Java 21 (JDK)
- Node.js LTS (>= 18)
- Docker + Docker Compose

## Démarrage Dev

### 1) Base de données (Docker Compose)
La configuration utilise PostgreSQL sur le port 5434.
```bash
cd infra
docker compose up -d
```

### 2) Backend (API)
Configuration par défaut: voir `backend/src/main/resources/application.yml`.
```bash
cd backend
mvn -DskipTests package
java -jar target/backend-0.1.0.jar
# ou
mvn spring-boot:run
```
API: http://localhost:8080

### 3) Frontend (Web)
```bash
cd frontend
npm install
npm run dev
```
Frontend: http://localhost:3000

## Configuration (dev)
Backend (`backend/src/main/resources/application.yml`)
- `spring.datasource.url`: `jdbc:postgresql://127.0.0.1:5434/ticketing`
- `spring.datasource.username`: `ticket`
- `spring.datasource.password`: `ticketpwd`
- `app.jwt.accessSecret` / `app.jwt.refreshSecret`: secrets à définir
- `app.stripe.secret`: lu depuis `STRIPE_SECRET_KEY` (par défaut `sk_test_xxx`)
- `app.stripe.webhook-secret`: lu depuis `STRIPE_WEBHOOK_SECRET` (par défaut `whsec_xxx`)
- `app.stripe.skip-verify`: lu depuis `STRIPE_SKIP_VERIFY` (par défaut `true` en dev)
- `app.stripe.fake`: lu depuis `STRIPE_FAKE` (par défaut `true` en dev, à passer à `false` en prod)

Frontend (`frontend/.env.local`)
- `NEXT_PUBLIC_API_BASE=http://localhost:8080`
 - `NEXT_PUBLIC_STRIPE_PK`=clé publique Stripe (test ou live, pour Stripe Elements)
 - `NEXT_PUBLIC_STRIPE_DEV_MODE`=`true` pour afficher les outils de simulation (fake) en dev, omettre ou mettre `false` en préprod/prod

## Endpoints clés
- `GET /api/health` — ping
- `GET /api/events` / `GET /api/events/{id}` — événements (UUID, DB)
- `POST /api/orders` — créer (requiert USER)
- `GET /api/orders` / `GET /api/orders/{id}` — lister/détails
- `PATCH /api/orders/{id}` — modifier quantité (PENDING)
- `PATCH /api/orders/{id}/cancel` — annuler (non PAID)
- `POST /api/payments/stripe/webhook` — webhook Stripe (skip-verify en dev)

### Webhook Stripe (configuration)

- URL à déclarer dans Stripe: `https://<votre-domaine>/api/payments/stripe/webhook`
- Types d'événements utilisés: `payment_intent.succeeded`, `payment_intent.payment_failed`
- En développement:
	- `STRIPE_FAKE=true` (pas d'appel réel à Stripe, PaymentIntent simulé)
	- `STRIPE_SKIP_VERIFY=true` (signature non vérifiée, utile pour tests locaux/Postman)
- En production/préproduction:
	- `STRIPE_FAKE=false` (appels réels à Stripe)
	- `STRIPE_SKIP_VERIFY=false` (oblige la présence de l'en-tête `Stripe-Signature` et vérification avec `STRIPE_WEBHOOK_SECRET`)
	- `STRIPE_SECRET_KEY` et `STRIPE_WEBHOOK_SECRET` configurés avec les valeurs Stripe (test ou live)

### Passer en mode Stripe test réel (recommandé avant la prod)

1. Dans Stripe Dashboard, récupérer:
	- la clé secrète test (`STRIPE_SECRET_KEY`),
	- la clé publique test (`NEXT_PUBLIC_STRIPE_PK`),
	- le secret de webhook test (`STRIPE_WEBHOOK_SECRET`).
2. Côté backend (env ou docker compose):
	- `STRIPE_SECRET_KEY=<clé secrète test>`
	- `STRIPE_WEBHOOK_SECRET=<secret webhook test>`
	- `STRIPE_FAKE=false`
	- `STRIPE_SKIP_VERIFY=false`
3. Côté frontend (`frontend/.env.local`):
	- `NEXT_PUBLIC_STRIPE_PK=<clé publique test>`
	- `NEXT_PUBLIC_STRIPE_DEV_MODE=false` (ou supprimer la variable)
4. Dans Stripe Dashboard, déclarer l'URL de webhook:
	- `https://<votre-domaine>/api/payments/stripe/webhook` (ou `http://localhost:8080/...` via Stripe CLI en local).
5. Vérifier le flux complet: création commande → PaymentIntent via UI → saisie carte test dans Stripe Elements → réception de l'événement webhook → paiement `SUCCESS` et billet généré.

## Branches
- `main` — stable
- `develop` — intégration
- `feature/stripe-dev-mode` — mode Stripe dev + couverture front

## Notes
- Sécurité: stateless JWT + rôles.
- Migrations: Flyway (`backend/src/main/resources/db/migration`).
- Dev Stripe: `fake=true` et `skip-verify=true` pour accélérer les tests.
