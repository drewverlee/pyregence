-- NAMESPACE: user

-- Stores information about users
CREATE TABLE users (
    user_uid       SERIAL PRIMARY KEY,
    email          text NOT NULL UNIQUE,
    name           text NOT NULL,
    password       varchar(72) NOT NULL,
    settings       text,
    super_admin    boolean DEFAULT FALSE,
    verified       boolean DEFAULT FALSE,
    reset_key      text DEFAULT NULL
);

-- Stores information about organizations
CREATE TABLE organizations (
    organization_uid    SERIAL PRIMARY KEY,
    org_name            text NOT NULL,
    email_domains       text,
    settings            text,
    archived            boolean DEFAULT FALSE,
    created_date        date DEFAULT NOW(),
    archived_date       date
);

-- Stores text values for roles
CREATE TABLE roles (
    role_uid    SERIAL PRIMARY KEY,
    title       text NOT NULL
);

-- Creates a relationship between users and organizations
-- organizations -> many organization_users <- users
CREATE TABLE organization_users (
    org_user_uid        SERIAL PRIMARY KEY,
    organization_rid    integer NOT NULL REFERENCES organizations (organization_uid) ON DELETE CASCADE ON UPDATE CASCADE,
    user_rid            integer NOT NULL REFERENCES users (user_uid) ON DELETE CASCADE ON UPDATE CASCADE,
    role_rid            integer NOT NULL REFERENCES roles (role_uid),
    CONSTRAINT per_organization_per_user UNIQUE(organization_rid, user_rid)
);