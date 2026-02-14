# PR: JPA – Validation du schéma

## Objectif
Assurer la cohérence entre les entités JPA et le schéma Flyway via `ddl-auto: validate`.

## Changements clés
- Mise à jour de `application.yml` (`spring.jpa.hibernate.ddl-auto: validate`).

## Tests / Vérifications
- Démarrer l’app, vérifier qu’aucune erreur de mapping n’est levée.
- Fumigène: appels `GET /api/health`, `GET /api/events`.

## Risques et rollback
- Risque faible: si un mapping est invalide, l’app ne démarre pas → corriger entités/migrations.
