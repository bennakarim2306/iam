-- ============================================================================
-- FOODOPIA DATABASE SCHEMA - Cleaned and Consolidated
-- ============================================================================

-- Create Schemas
CREATE SCHEMA IF NOT EXISTS business_data;
CREATE SCHEMA IF NOT EXISTS user_details;

-- ============================================================================
-- ITEMS TABLE - Product Offers
-- ============================================================================
CREATE TABLE IF NOT EXISTS business_data.items (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    unit VARCHAR(50) NOT NULL,

    -- Address Information
    address_street VARCHAR(255),
    address_city VARCHAR(255),
    address_zip VARCHAR(20),
    address_lat DOUBLE PRECISION,
    address_lng DOUBLE PRECISION,

    -- Seller Information
    seller_id VARCHAR(255),
    seller_name VARCHAR(255),
    seller_contact VARCHAR(255),

    -- Availability
    availableFrom DATE,
    availableTo DATE,

    -- Additional Info
    description TEXT,
    imageUrl TEXT,
    thumbnail_url TEXT,

    -- Optimistic Locking
    version BIGINT DEFAULT 0,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices for Performance
CREATE INDEX IF NOT EXISTS idx_items_seller_contact ON business_data.items(seller_contact);
CREATE INDEX IF NOT EXISTS idx_items_type ON business_data.items(type);
CREATE INDEX IF NOT EXISTS idx_items_address_city ON business_data.items(address_city);

-- ============================================================================
-- USERS TABLE - User Details
-- ============================================================================
CREATE TABLE IF NOT EXISTS user_details."user-details" (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    password VARCHAR(255),
    age INTEGER,
    birth_day DATE,
    role VARCHAR(255) CHECK (role IN ('USER', 'ADMIN')),
    consent_allowed BOOLEAN NOT NULL,

    -- Contacts Management
    contacts_list VARCHAR(255),
    contacts_request_list VARCHAR(255),

    -- Address Information
    address_street VARCHAR(255),
    address_city VARCHAR(255),
    address_zip VARCHAR(20),
    address_lat DOUBLE PRECISION,
    address_lng DOUBLE PRECISION,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices for Performance
CREATE INDEX IF NOT EXISTS idx_users_email ON user_details."user-details"(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON user_details."user-details"(role);

-- ============================================================================
-- TRANSACTIONS TABLE - Orders/Transactions
-- ============================================================================
CREATE TABLE IF NOT EXISTS business_data.transactions (
    id VARCHAR(255) PRIMARY KEY,

    -- Item Reference
    item_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (item_id) REFERENCES business_data.items(id) ON DELETE CASCADE,

    -- Customer and Seller Information
    customer_email VARCHAR(255) NOT NULL,
    seller_email VARCHAR(255) NOT NULL,

    -- Order Details
    quantity_ordered DOUBLE PRECISION NOT NULL,
    price_per_unit DOUBLE PRECISION NOT NULL,
    total_price DOUBLE PRECISION NOT NULL,

    -- Status Management
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'REJECTED')),

    -- Additional Information
    notes TEXT,

    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices for Performance and Common Queries
CREATE INDEX IF NOT EXISTS idx_transactions_customer_email ON business_data.transactions(customer_email);
CREATE INDEX IF NOT EXISTS idx_transactions_seller_email ON business_data.transactions(seller_email);
CREATE INDEX IF NOT EXISTS idx_transactions_item_id ON business_data.transactions(item_id);
CREATE INDEX IF NOT EXISTS idx_transactions_status ON business_data.transactions(status);
CREATE INDEX IF NOT EXISTS idx_transactions_created_at ON business_data.transactions(created_at);

-- ============================================================================
-- Schema Update: Add Missing Columns if they Don't Exist
-- ============================================================================

-- Add version field to items if it doesn't exist
ALTER TABLE business_data.items
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Add timestamps to items if they don't exist
ALTER TABLE business_data.items
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add timestamps to users if they don't exist
ALTER TABLE user_details."user-details"
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Ensure imageUrl in items can handle large base64 encoded images
ALTER TABLE business_data.items
    ALTER COLUMN imageurl TYPE TEXT;

-- ============================================================================
-- ALTER TABLE STATEMENTS - Add thumbnail support to items
-- ============================================================================

-- Füge thumbnail_url Spalte zur items Tabelle hinzu (falls noch nicht vorhanden)
ALTER TABLE business_data.items ADD COLUMN IF NOT EXISTS thumbnail_url TEXT;

-- ============================================================================
-- ALTER TABLE STATEMENTS - Add Profile & Availability Management to users
-- ============================================================================

-- Add profile picture (Base64 encoded image)
ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS profile_picture TEXT;

-- Add availability days (e.g., "MON,TUE,WED,THU,FRI" or pipe-separated)
ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS availability_days VARCHAR(255);

-- Add availability times (e.g., "09:00-17:00" or multiple time spans pipe-separated)
ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS availability_times VARCHAR(255);

-- Add flag whether user requests confirmation for bookings
ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS need_confirmation BOOLEAN DEFAULT FALSE;

-- Add flag whether user profile is complete
ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS profile_complete BOOLEAN DEFAULT FALSE;

ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS created_at VARCHAR(255);

ALTER TABLE user_details."user-details" ADD COLUMN IF NOT EXISTS updated_at VARCHAR(255);


    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        available_from date,
        available_to date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageUrl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );

    create table business_data.items (
        address_lat float(53),
        address_lng float(53),
        availablefrom date,
        availableto date,
        price float(53) not null,
        quantity float(53) not null,
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        description varchar(255),
        id varchar(255) not null,
        imageurl varchar(255),
        name varchar(255),
        seller_contact varchar(255),
        seller_id varchar(255),
        seller_name varchar(255),
        thumbnail_url varchar(255),
        type varchar(255),
        unit varchar(255),
        primary key (id)
    );

    create table business_data.transactions (
        price_per_unit float(53) not null,
        quantity_ordered float(53) not null,
        total_price float(53) not null,
        created_at timestamp(6) not null,
        updated_at timestamp(6) not null,
        customer_email varchar(255) not null,
        id varchar(255) not null,
        item_id varchar(255) not null,
        notes TEXT,
        seller_email varchar(255) not null,
        status varchar(255) not null check (status in ('PENDING','CONFIRMED','COMPLETED','CANCELLED','REJECTED')),
        primary key (id)
    );

    create table user_details."user-details" (
        address_lat float(53),
        address_lng float(53),
        age integer,
        birth_day date,
        consent_allowed boolean not null,
        need_confirmation boolean,
        profile_complete boolean,
        created_at timestamp(6),
        updated_at timestamp(6),
        address_city varchar(255),
        address_street varchar(255),
        address_zip varchar(255),
        availability_days varchar(255),
        availability_times varchar(255),
        contacts_list varchar(255),
        contacts_request_list varchar(255),
        email varchar(255),
        first_name varchar(255),
        id varchar(255) not null,
        last_name varchar(255),
        password varchar(255),
        profile_picture varchar(255),
        role varchar(255) check (role in ('USER','ADMIN')),
        primary key (id)
    );
