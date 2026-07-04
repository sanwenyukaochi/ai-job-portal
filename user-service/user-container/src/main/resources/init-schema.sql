DROP SCHEMA IF EXISTS user_identity CASCADE;

CREATE SCHEMA user_identity;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE user_identity.user_accounts (
    id uuid NOT NULL,
    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role varchar(50) NOT NULL,
    auth_provider varchar(50) NOT NULL,
    status varchar(50) NOT NULL,
    verified boolean NOT NULL DEFAULT false,
    token_version integer NOT NULL DEFAULT 0,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    last_login_at timestamp with time zone NULL,
    suspended_at timestamp with time zone NULL,
    deleted_at timestamp with time zone NULL,
    password_updated_at timestamp with time zone NOT NULL,
    CONSTRAINT user_accounts_pkey PRIMARY KEY (id),
    CONSTRAINT user_accounts_email_uk UNIQUE (email)
);

CREATE TABLE user_identity.user_profiles (
    user_id uuid NOT NULL,
    full_name varchar(255) NOT NULL,
    phone varchar(50) NULL,
    avatar_url varchar(512) NULL,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone NOT NULL,
    CONSTRAINT user_profiles_pkey PRIMARY KEY (user_id),
    CONSTRAINT user_profiles_user_id_fk
        FOREIGN KEY (user_id) REFERENCES user_identity.user_accounts (id)
);

CREATE INDEX idx_user_accounts_role ON user_identity.user_accounts (role);
CREATE INDEX idx_user_accounts_status ON user_identity.user_accounts (status);
