# PR: Migrations Flyway – Schéma initial (V1)

## Objectif
Mettre en place un schéma initial des tables et activer Flyway avec une datasource dédiée pour des migrations reproductibles.

## Changements clés

## Tests / Vérifications
# PR: Migrations Flyway – Schéma initial (V1)

## Objectif
Mettre en place un schéma initial des tables et activer Flyway avec une datasource dédiée pour des migrations reproductibles.

## Changements clés
- Ajout de `V1__schema.sql` avec tables `users`, `events`, `orders`, `tickets`, `payments`, `refunds` et index.
- Activation de Flyway (`enabled: true`) et configuration de la datasource Flyway dans `application.yml`.
- JPA réglé sur `ddl-auto: none` pour éviter les conflits au démarrage.
- Ajout du module `flyway-database-postgresql` dans `pom.xml`.

## Tests / Vérifications
- Démarrer l’application: `mvn -DskipTests spring-boot:run`.
- Vérifier l’historique: `SELECT version, description FROM flyway_schema_history;`.
- Vérifier tables et quelques `COUNT(*)` (events/users/orders/tickets/payments).

## Risques et rollback
- Risque faible: migrations déterministes. Rollback: revert la PR et désactiver Flyway si besoin.
