# Palais de la Culture – Plateforme de Billetterie (Monorepo)

Ce dépôt regroupe le backend (Spring Boot), le frontend (Next.js) et l'infra (Docker Compose) pour la billetterie digitale.

## Structure
- backend/ — API REST Spring Boot (Java 17)
- frontend/ — Application web Next.js (React + TypeScript)
- infra/ — Docker Compose (PostgreSQL)
- docs/ — Spécifications et diagrammes (optionnel)

## Prérequis
- Java 17 (JDK)
- Node.js LTS (>= 18)
- Docker + Docker Compose
- PostgreSQL (via Docker)

## Démarrage rapide

### 1) Infra (base de données)
```bash
cd infra
docker compose up -d
```

### 2) Backend (API)
```bash
cd backend
mvn spring-boot:run
```
API par défaut: http://localhost:8080

### 3) Frontend (Web)
```bash
cd frontend
npm install
npm run dev
```
Frontend: http://localhost:3000

## Variables d'environnement
Backend (`backend/src/main/resources/application.yml`) utilise:
- `DB_URL` (ex: jdbc:postgresql://localhost:5432/ticketing)
- `DB_USERNAME`
- `DB_PASSWORD`
- `STRIPE_SECRET`

Frontend (`frontend/.env.local`):
- `NEXT_PUBLIC_API_URL` (ex: http://localhost:8080)

## Branches recommandées

Branches long-lived:
- `main` — stable, versions livrées
- `develop` — intégration continue (préprod)

Backend (features):
- `backend/feature-auth`
- `backend/feature-events`
- `backend/feature-orders-payments`
- `backend/feature-tickets-qr`
- `backend/feature-notifications`

Frontend (features):
- `frontend/feature-auth`
- `frontend/feature-events`
- `frontend/feature-checkout`
- `frontend/feature-dashboards`

Branches release/hotfix:
- `backend/release-x.y`
- `frontend/release-x.y`
- `hotfix/...` (applicable front ou back)

Script d'initialisation des branches: `scripts/init-branches.sh`

## Endpoints (squelette)
- `GET /api/health` — ping
- `GET /api/events` — liste des événements (stub)
- `POST /api/orders` — création de commande (stub)
- `POST /api/payments/webhook` — webhook Stripe (stub)

## Notes
Ce dépôt est une base prête à étendre. Les contrôleurs renvoient des réponses simples (JSON) pour démarrer, à affiner avec la logique métier, la sécurité JWT et la persistance.
