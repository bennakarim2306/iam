-- Schemata
CREATE SCHEMA IF NOT EXISTS business_data;
CREATE SCHEMA IF NOT EXISTS user_details;

-- business_data.items nach neuem Modell
CREATE TABLE IF NOT EXISTS business_data.items (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL,
    address_street VARCHAR(255),
    address_city VARCHAR(255),
    address_zip VARCHAR(20),
    address_lat DOUBLE PRECISION,
    address_lng DOUBLE PRECISION,
    seller_id VARCHAR(255),
    seller_name VARCHAR(255),
    seller_contact VARCHAR(255),
    availableFrom DATE,
    availableTo DATE,
    description TEXT,
    imageUrl VARCHAR(512)
);

-- user_details."user-details" nach aktuellem Modell
CREATE TABLE IF NOT EXISTS user_details."user-details" (
    age INTEGER,
    birth_day DATE,
    consent_allowed BOOLEAN NOT NULL,
    contacts_list VARCHAR(255),
    contacts_request_list VARCHAR(255),
    email VARCHAR(255),
    first_name VARCHAR(255),
    id VARCHAR(255) NOT NULL,
    last_name VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255) CHECK (role IN ('USER','ADMIN')),
    PRIMARY KEY (id)
);

-- Add address fields to user-details (preserve evolution; IF NOT EXISTS to be safe)
ALTER TABLE user_details."user-details"
    ADD COLUMN IF NOT EXISTS address_street VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_city VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address_zip VARCHAR(20),
    ADD COLUMN IF NOT EXISTS address_lat DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS address_lng DOUBLE PRECISION;

-- End of create.sql
