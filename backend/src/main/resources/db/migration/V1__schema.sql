-- V1 Schema migration: create core tables and indexes
-- Consistent snake_case columns to match Spring Boot's default naming strategy.

-- Users
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    password_hash VARCHAR(255),
    role VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL
);

-- Events
CREATE TABLE IF NOT EXISTS events (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    start_date_time TIMESTAMPTZ NOT NULL,
    end_date_time TIMESTAMPTZ NOT NULL,
    venue VARCHAR(255),
    capacity INTEGER,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_events_createdat ON events (created_at);
CREATE INDEX IF NOT EXISTS idx_events_start_end ON events (start_date_time, end_date_time);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    amount DOUBLE PRECISION,
    status VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_orders_event FOREIGN KEY (event_id) REFERENCES events (id)
);
CREATE INDEX IF NOT EXISTS idx_orders_status_createdat ON orders (status, created_at);
CREATE INDEX IF NOT EXISTS idx_orders_createdat ON orders (created_at);
CREATE INDEX IF NOT EXISTS idx_orders_event_id ON orders (event_id);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders (user_id);

-- Tickets
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    order_id UUID NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_tickets_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
CREATE INDEX IF NOT EXISTS idx_tickets_status_createdat ON tickets (status, created_at);
CREATE INDEX IF NOT EXISTS idx_tickets_createdat ON tickets (created_at);
CREATE INDEX IF NOT EXISTS idx_tickets_event_id ON tickets (event_id);
CREATE INDEX IF NOT EXISTS idx_tickets_order_id ON tickets (order_id);
CREATE INDEX IF NOT EXISTS idx_tickets_user_id ON tickets (user_id);

-- Payments
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount DOUBLE PRECISION,
    status VARCHAR(50),
    provider VARCHAR(100),
    provider_payment_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id)
);
CREATE INDEX IF NOT EXISTS idx_payments_status_createdat ON payments (status, created_at);
CREATE INDEX IF NOT EXISTS idx_payments_createdat ON payments (created_at);
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments (order_id);

-- Refunds
CREATE TABLE IF NOT EXISTS refunds (
    id UUID PRIMARY KEY,
    payment_id UUID NOT NULL,
    amount DOUBLE PRECISION,
    status VARCHAR(50),
    provider VARCHAR(100),
    provider_refund_id VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
);
CREATE INDEX IF NOT EXISTS idx_refunds_status_createdat ON refunds (status, created_at);
CREATE INDEX IF NOT EXISTS idx_refunds_createdat ON refunds (created_at);
CREATE INDEX IF NOT EXISTS idx_refunds_payment_id ON refunds (payment_id);
