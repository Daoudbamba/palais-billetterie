# PR: Paiement Stripe – Mode Dev / Fake

## Objectif
Permettre des tests locaux complets sans dépendances externes, via:
- Mode fake PaymentIntent (IDs factices, `clientSecret` généré localement).
- Contournement de la vérification de signature de webhook pour le dev.

## Changements clés
- `app.stripe.fake: true` et `app.stripe.webhook-skip-verify: true` dans `application.yml`.
- `StripeService`: génération d’Intent factice si fake activé.
- `StripeWebhookController`: parsing JSON en dev sans `Webhook.constructEvent`.

### Sécurité
- Ajout d’un `PasswordEncoder` (BCrypt) dans `SecurityConfig` pour encoder/vérifier les mots de passe.

### Migrations
- Suppression d’un doublon Flyway `V1__seed_demo.sql` qui provoquait « Found more than one migration with version 1 ».

## Tests / Vérifications
- Créer commande (Bearer) → `orderId`.
- `POST /api/payments/stripe/create-intent?orderId=...` → `paymentId` + `clientSecret`.
- Simuler webhook succès → `payments.status=SUCCESS`, `orders.status=PAID`, ticket créé.

## Risques et rollback
- Aucun en prod (flags désactivés). En dev, rollback simple en mettant les flags à `false`.

## Base et ordre
- Base de PR recommandée: `develop`.
