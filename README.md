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
- `app.stripe.secret`: clé Stripe (dev)
- `app.stripe.webhook-secret`: secret webhook (dev)
- `app.stripe.skip-verify`: `true` en dev (ne pas vérifier la signature)
- `app.stripe.fake`: `true` en dev (intent Stripe simulé)

Frontend (`frontend/.env.local`)
- `NEXT_PUBLIC_API_BASE=http://localhost:8080`

## Endpoints clés
- `GET /api/health` — ping
- `GET /api/events` / `GET /api/events/{id}` — événements (UUID, DB)
- `POST /api/orders` — créer (requiert USER)
- `GET /api/orders` / `GET /api/orders/{id}` — lister/détails
- `PATCH /api/orders/{id}` — modifier quantité (PENDING)
- `PATCH /api/orders/{id}/cancel` — annuler (non PAID)
- `POST /api/payments/stripe/webhook` — webhook Stripe (skip-verify en dev)

## Branches
- `main` — stable
- `develop` — intégration
- `feature/stripe-dev-mode` — mode Stripe dev + couverture front

## Notes
- Sécurité: stateless JWT + rôles.
- Migrations: Flyway (`backend/src/main/resources/db/migration`).
- Dev Stripe: `fake=true` et `skip-verify=true` pour accélérer les tests.
