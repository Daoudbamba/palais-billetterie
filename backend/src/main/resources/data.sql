-- Seed demo events with explicit UUIDs
INSERT INTO events (id, title, description, start_date_time, end_date_time, venue, capacity, created_at)
VALUES ('5f4a3c7e-6a2b-4b6a-bf2f-3d6b9f1e2a10', 'Concert Jazz', 'Concert jazz au Palais', '2026-03-01 19:00:00+00', '2026-03-01 21:00:00+00', 'Grande Salle', 500, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO events (id, title, description, start_date_time, end_date_time, venue, capacity, created_at)
VALUES ('9c1e2d3f-7b8a-4c9d-8e1f-2a3b4c5d6e7f', 'Conférence Tech', 'Conférence sur l’innovation technologique', '2026-03-15 09:30:00+00', '2026-03-15 12:00:00+00', 'Auditorium', 300, now())
ON CONFLICT (id) DO NOTHING;

-- Seed demo users
INSERT INTO users (id, name, email, phone, role, created_at)
VALUES ('a1b2c3d4-e5f6-4711-8899-000000000001', 'Alice Martin', 'alice@example.com', '+33123456789', 'USER', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO users (id, name, email, phone, role, created_at)
VALUES ('b2c3d4e5-f6a7-4822-9900-000000000002', 'Bob Dupont', 'bob@example.com', '+33987654321', 'ADMIN', now())
ON CONFLICT (id) DO NOTHING;

-- Seed demo orders (link seeded users and events)
INSERT INTO orders (id, user_id, event_id, amount, status, created_at)
VALUES ('c1d2e3f4-1111-2222-3333-444455556666', 'a1b2c3d4-e5f6-4711-8899-000000000001', '5f4a3c7e-6a2b-4b6a-bf2f-3d6b9f1e2a10', 49.99, 'PENDING', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO orders (id, user_id, event_id, amount, status, created_at)
VALUES ('d2e3f4a5-7777-8888-9999-aaaabbbbcccc', 'b2c3d4e5-f6a7-4822-9900-000000000002', '9c1e2d3f-7b8a-4c9d-8e1f-2a3b4c5d6e7f', 79.00, 'PAID', now())
ON CONFLICT (id) DO NOTHING;

-- Seed demo tickets (link seeded users/events/orders)
INSERT INTO tickets (id, user_id, event_id, order_id, code, status, created_at)
VALUES ('e3f4a5b6-1234-5678-9abc-def012345678', 'a1b2c3d4-e5f6-4711-8899-000000000001', '5f4a3c7e-6a2b-4b6a-bf2f-3d6b9f1e2a10', 'c1d2e3f4-1111-2222-3333-444455556666', 'TCK-2026-0001', 'VALID', now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO tickets (id, user_id, event_id, order_id, code, status, created_at)
VALUES ('f4a5b6c7-2345-6789-abcd-ef0123456789', 'b2c3d4e5-f6a7-4822-9900-000000000002', '9c1e2d3f-7b8a-4c9d-8e1f-2a3b4c5d6e7f', 'd2e3f4a5-7777-8888-9999-aaaabbbbcccc', 'TCK-2026-0002', 'USED', now())
ON CONFLICT (id) DO NOTHING;

-- Seed demo payments
INSERT INTO payments (id, order_id, amount, status, provider, provider_payment_id, created_at)
VALUES ('aa11bb22-cc33-dd44-ee55-ff6677889900', 'c1d2e3f4-1111-2222-3333-444455556666', 49.99, 'PENDING', 'STRIPE', NULL, now())
ON CONFLICT (id) DO NOTHING;

INSERT INTO payments (id, order_id, amount, status, provider, provider_payment_id, created_at)
VALUES ('bb22cc33-dd44-ee55-ff66-77889900aa11', 'd2e3f4a5-7777-8888-9999-aaaabbbbcccc', 79.00, 'SUCCESS', 'STRIPE', 'pi_demo_001', now())
ON CONFLICT (id) DO NOTHING;
