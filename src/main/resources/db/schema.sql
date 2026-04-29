BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    country TEXT NOT NULL,
    city TEXT NOT NULL,
    street TEXT NOT NULL,
    number TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    address_id UUID NOT NULL REFERENCES addresses(id) ON DELETE RESTRICT,
    email TEXT NOT NULL,
    phone TEXT NOT NULL,
    is_company BOOLEAN NOT NULL,
    vat_number TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS drone_hubs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    address_id UUID NOT NULL REFERENCES addresses(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS drones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hub_id UUID NULL REFERENCES drone_hubs(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    flight_range INTEGER NOT NULL,
    maximum_payload DOUBLE PRECISION NOT NULL,
    maximum_speed DOUBLE PRECISION NOT NULL,
    is_available BOOLEAN NOT NULL,
    last_maintenance DATE NOT NULL,
    current_load DOUBLE PRECISION NOT NULL DEFAULT 0,

    -- cargo drone specific field
    has_refrigerator BOOLEAN NULL,

    CONSTRAINT drones_type_check CHECK (type IN ('NORMAL', 'CARGO', 'HIGH_SPEED'))
);

CREATE TABLE IF NOT EXISTS personnel (
    id UUID PRIMARY KEY,
    hub_id UUID NULL REFERENCES drone_hubs(id) ON DELETE SET NULL,
    full_name TEXT NOT NULL,
    certification TEXT NOT NULL,
    is_available BOOLEAN NOT NULL,

    CONSTRAINT personnel_certification_check CHECK (certification IN ('NONE', 'MECHANIC', 'OPERATOR', 'COMMANDER'))
);

CREATE TABLE IF NOT EXISTS packages (
    id UUID PRIMARY KEY,
    weight DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    length DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL
);

CREATE TABLE IF NOT EXISTS package_requirements (
    package_id UUID NOT NULL REFERENCES packages(id) ON DELETE CASCADE,
    requirement TEXT NOT NULL,
    PRIMARY KEY (package_id, requirement),

    CONSTRAINT package_requirement_check CHECK (requirement IN ('NONE', 'REFRIGERATED', 'EXPRESS_DELIVERY', 'FRAGILE', 'HAZARDOUS'))
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    sender_contact_id UUID NOT NULL REFERENCES contacts(id) ON DELETE RESTRICT,
    receiver_contact_id UUID NOT NULL REFERENCES contacts(id) ON DELETE RESTRICT,
    package_id UUID NOT NULL REFERENCES packages(id) ON DELETE RESTRICT,
    status TEXT NOT NULL,
    hub_id UUID NULL REFERENCES drone_hubs(id) ON DELETE SET NULL,
    assigned_drone_id UUID NULL REFERENCES drones(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    delivered_at TIMESTAMPTZ NULL,

    CONSTRAINT orders_status_check CHECK (status IN ('UNCOLLECTED', 'IN_HUB', 'ASSIGNED', 'DELIVERED'))
);

CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_hub_id ON orders(hub_id);
CREATE INDEX IF NOT EXISTS idx_drones_hub_id ON drones(hub_id);
CREATE INDEX IF NOT EXISTS idx_personnel_hub_id ON personnel(hub_id);

COMMIT;
